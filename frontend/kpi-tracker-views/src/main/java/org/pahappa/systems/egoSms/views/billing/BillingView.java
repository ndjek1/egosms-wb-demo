package org.pahappa.systems.egoSms.views.billing;

import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import org.pahappa.systems.egoSms.core.services.billing.WhatsappBillingService;
import org.pahappa.systems.egoSms.models.billing.*;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

@ManagedBean(name = "billingView")
@ViewScoped
public class BillingView implements Serializable {
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
    accounts = service.listCurrentUserAccounts();
    invoices = service.listCurrentUserInvoices();
  }

  public List<WhatsappBillingAccount> getAccounts() {
    return accounts;
  }

  public List<WhatsappBillingInvoice> getInvoices() {
    return invoices;
  }
}
