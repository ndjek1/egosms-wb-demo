package org.pahappa.systems.egoSms.core.services.impl.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.pahappa.systems.egoSms.core.services.billing.WhatsappBillingService;
import org.pahappa.systems.egoSms.models.billing.BillingEnums;
import org.pahappa.systems.egoSms.models.billing.WhatsappBillingAccount;
import org.pahappa.systems.egoSms.models.billing.WhatsappBillingInvoice;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappEnums;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WhatsappBillingServiceImpl implements WhatsappBillingService {
  @PersistenceContext private EntityManager em;

  public List<WhatsappBillingAccount> listAllAccounts() {
    requireAdmin();
    provisionAll();
    return em.createQuery(
            "select b from WhatsappBillingAccount b join fetch b.connection c "
                + "join fetch c.owner where b.recordStatus=:s order by c.verifiedName",
            WhatsappBillingAccount.class)
        .setParameter("s", RecordStatus.ACTIVE)
        .getResultList();
  }

  public List<WhatsappBillingAccount> listCurrentUserAccounts() {
    User user = user();
    provisionUser(user);
    return em.createQuery(
            "select b from WhatsappBillingAccount b join fetch b.connection c "
                + "where c.owner=:u and b.recordStatus=:s order by c.verifiedName",
            WhatsappBillingAccount.class)
        .setParameter("u", user)
        .setParameter("s", RecordStatus.ACTIVE)
        .getResultList();
  }

  public List<WhatsappBillingInvoice> listCurrentUserInvoices() {
    User user = user();
    provisionUser(user);
    return em.createQuery(
            "select i from WhatsappBillingInvoice i join fetch i.billingAccount b "
                + "join fetch b.connection c where c.owner=:u and i.recordStatus=:s "
                + "order by i.periodKey desc",
            WhatsappBillingInvoice.class)
        .setParameter("u", user)
        .setParameter("s", RecordStatus.ACTIVE)
        .getResultList();
  }

  public List<WhatsappBillingInvoice> listAllInvoices() {
    requireAdmin();
    return em.createQuery(
            "select i from WhatsappBillingInvoice i join fetch i.billingAccount b "
                + "join fetch b.connection c join fetch c.owner where i.recordStatus=:s "
                + "order by i.periodKey desc,c.verifiedName",
            WhatsappBillingInvoice.class)
        .setParameter("s", RecordStatus.ACTIVE)
        .getResultList();
  }

  public WhatsappBillingAccount saveAccount(WhatsappBillingAccount account) {
    requireAdmin();
    validate(account);
    return em.merge(account);
  }

  public WhatsappBillingInvoice generateCurrentInvoice(String id) {
    requireAdmin();
    WhatsappBillingAccount account = account(id);
    String period = new SimpleDateFormat("yyyy-MM").format(new Date());
    Date[] range = range();
    Long count =
        em.createQuery(
                "select count(m) from WhatsappMessage m where m.conversation.connection=:c "
                    + "and m.direction=:d and m.billingChargeable=:chargeable "
                    + "and m.sentAt>=:from and m.sentAt<:to",
                Long.class)
            .setParameter("c", account.getConnection())
            .setParameter("d", WhatsappEnums.Direction.OUTBOUND)
            .setParameter("chargeable", Boolean.TRUE)
            .setParameter("from", range[0])
            .setParameter("to", range[1])
            .getSingleResult();

    List<WhatsappBillingInvoice> rows =
        em.createQuery(
                "select i from WhatsappBillingInvoice i where i.billingAccount=:a "
                    + "and i.periodKey=:p",
                WhatsappBillingInvoice.class)
            .setParameter("a", account)
            .setParameter("p", period)
            .getResultList();
    WhatsappBillingInvoice invoice = rows.isEmpty() ? new WhatsappBillingInvoice() : rows.get(0);
    if (BillingEnums.InvoiceStatus.PAID == invoice.getInvoiceStatus()) {
      throw new IllegalStateException("A paid invoice cannot be recalculated");
    }

    BigDecimal meta =
        account
            .getEstimatedMetaCostPerMessage()
            .multiply(BigDecimal.valueOf(count))
            .setScale(4, RoundingMode.HALF_UP);
    BigDecimal markup =
        meta.multiply(account.getMarkupPercent())
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    invoice.setBillingAccount(account);
    invoice.setPeriodKey(period);
    invoice.setMessageCount(count);
    invoice.setEstimatedMetaCost(meta);
    invoice.setMarkupAmount(markup);
    invoice.setMonthlyFee(account.getMonthlyFee());
    invoice.setTotalAmount(
        meta.add(markup).add(account.getMonthlyFee()).setScale(4, RoundingMode.HALF_UP));
    invoice.setInvoiceStatus(BillingEnums.InvoiceStatus.OPEN);
    invoice.setGeneratedAt(new Date());
    invoice.setRecordStatus(RecordStatus.ACTIVE);
    if (rows.isEmpty()) {
      em.persist(invoice);
    } else {
      invoice = em.merge(invoice);
    }
    return invoice;
  }

  public void markPaid(String id) {
    requireAdmin();
    WhatsappBillingInvoice invoice = em.find(WhatsappBillingInvoice.class, id);
    if (invoice == null) {
      throw new IllegalArgumentException("Invoice was not found");
    }
    invoice.setInvoiceStatus(BillingEnums.InvoiceStatus.PAID);
    invoice.setPaidAt(new Date());
  }

  public void toggleSuspension(String id) {
    requireAdmin();
    WhatsappBillingAccount account = account(id);
    account.setAccountStatus(
        account.isSuspended()
            ? BillingEnums.AccountStatus.ACTIVE
            : BillingEnums.AccountStatus.SUSPENDED);
  }

  public void assertSendingAllowed(WhatsappConnection connection) {
    WhatsappBillingAccount account = find(connection);
    if (account != null && account.isSuspended()) {
      throw new IllegalStateException(
          "This WhatsApp account is suspended for billing. Contact an egoSms administrator.");
    }
  }

  private void provisionAll() {
    List<WhatsappConnection> connections =
        em.createQuery(
                "select c from WhatsappConnection c where c.recordStatus=:s",
                WhatsappConnection.class)
            .setParameter("s", RecordStatus.ACTIVE)
            .getResultList();
    for (WhatsappConnection connection : connections) {
      provision(connection);
    }
  }

  private void provisionUser(User user) {
    List<WhatsappConnection> connections =
        em.createQuery(
                "select c from WhatsappConnection c where c.owner=:u and c.recordStatus=:s",
                WhatsappConnection.class)
            .setParameter("u", user)
            .setParameter("s", RecordStatus.ACTIVE)
            .getResultList();
    for (WhatsappConnection connection : connections) {
      provision(connection);
    }
  }

  private WhatsappBillingAccount provision(WhatsappConnection connection) {
    WhatsappBillingAccount account = find(connection);
    if (account != null) {
      return account;
    }
    account = new WhatsappBillingAccount();
    account.setConnection(connection);
    account.setRecordStatus(RecordStatus.ACTIVE);
    em.persist(account);
    return account;
  }

  private WhatsappBillingAccount find(WhatsappConnection connection) {
    List<WhatsappBillingAccount> rows =
        em.createQuery(
                "select b from WhatsappBillingAccount b where b.connection=:c "
                    + "and b.recordStatus=:s",
                WhatsappBillingAccount.class)
            .setParameter("c", connection)
            .setParameter("s", RecordStatus.ACTIVE)
            .getResultList();
    return rows.isEmpty() ? null : rows.get(0);
  }

  private WhatsappBillingAccount account(String id) {
    WhatsappBillingAccount account = em.find(WhatsappBillingAccount.class, id);
    if (account == null) {
      throw new IllegalArgumentException("Billing account was not found");
    }
    return account;
  }

  private User user() {
    User user = SharedAppData.getLoggedInUser();
    if (user == null) {
      throw new IllegalStateException("A logged-in user is required");
    }
    return user;
  }

  private void requireAdmin() {
    if (!user().hasAdministrativePrivileges()) {
      throw new SecurityException("Administrator access is required");
    }
  }

  private void validate(WhatsappBillingAccount account) {
    if (account == null) {
      throw new IllegalArgumentException("Billing account is required");
    }
    if (account.getCurrency() == null || account.getCurrency().trim().length() != 3) {
      throw new IllegalArgumentException("Use a three-letter currency code");
    }
    if (negative(account.getMonthlyFee())
        || negative(account.getEstimatedMetaCostPerMessage())
        || negative(account.getMarkupPercent())) {
      throw new IllegalArgumentException("Billing rates cannot be negative");
    }
    account.setCurrency(account.getCurrency().trim().toUpperCase(Locale.ENGLISH));
  }

  private boolean negative(BigDecimal value) {
    return value == null || value.compareTo(BigDecimal.ZERO) < 0;
  }

  private Date[] range() {
    Calendar start = Calendar.getInstance();
    start.set(Calendar.DAY_OF_MONTH, 1);
    start.set(Calendar.HOUR_OF_DAY, 0);
    start.set(Calendar.MINUTE, 0);
    start.set(Calendar.SECOND, 0);
    start.set(Calendar.MILLISECOND, 0);
    Calendar end = (Calendar) start.clone();
    end.add(Calendar.MONTH, 1);
    return new Date[] {start.getTime(), end.getTime()};
  }
}
