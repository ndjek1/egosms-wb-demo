package org.pahappa.systems.egoSms.views.billing;

import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import org.pahappa.systems.egoSms.core.services.billing.WhatsappBillingService;
import org.pahappa.systems.egoSms.models.billing.*;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

@ManagedBean(name = "billingAdminView")
@ViewScoped
public class BillingAdminView implements Serializable {
  private static final long serialVersionUID = 1L;
  private WhatsappBillingService service;
  private List<WhatsappBillingAccount> accounts = Collections.emptyList();
  private List<WhatsappBillingInvoice> invoices = Collections.emptyList();

  @PostConstruct
  public void init() {
    service = ApplicationContextProvider.getBean(WhatsappBillingService.class);
    refresh();
  }

  public void refresh() {
    accounts = service.listAllAccounts();
    invoices = service.listAllInvoices();
  }

  public void save(WhatsappBillingAccount a) {
    try {
      service.saveAccount(a);
      refresh();
      UiUtils.showMessageBox("Billing updated", "The simulated rates were saved");
    } catch (Exception e) {
      UiUtils.ComposeFailure("Could not update billing", e.getMessage());
    }
  }

  public void generate(WhatsappBillingAccount a) {
    try {
      service.generateCurrentInvoice(a.getId());
      refresh();
      UiUtils.showMessageBox("Invoice generated", "Current-month usage was recalculated");
    } catch (Exception e) {
      UiUtils.ComposeFailure("Could not generate invoice", e.getMessage());
    }
  }

  public void toggle(WhatsappBillingAccount a) {
    try {
      service.toggleSuspension(a.getId());
      refresh();
      UiUtils.showMessageBox(
          "Account updated",
          a.isSuspended() ? "Sending has been restored" : "Sending has been suspended");
    } catch (Exception e) {
      UiUtils.ComposeFailure("Could not update account", e.getMessage());
    }
  }

  public void paid(WhatsappBillingInvoice i) {
    try {
      service.markPaid(i.getId());
      refresh();
      UiUtils.showMessageBox("Payment recorded", "The simulated invoice is now paid");
    } catch (Exception e) {
      UiUtils.ComposeFailure("Could not record payment", e.getMessage());
    }
  }

  public List<WhatsappBillingAccount> getAccounts() {
    return accounts;
  }

  public List<WhatsappBillingInvoice> getInvoices() {
    return invoices;
  }
}
