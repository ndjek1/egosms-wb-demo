package org.pahappa.systems.egoSms.core.services.billing;

import java.util.List;
import org.pahappa.systems.egoSms.models.billing.*;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;

public interface WhatsappBillingService {
  List<WhatsappBillingAccount> listAllAccounts();

  List<WhatsappBillingAccount> listCurrentUserAccounts();

  List<WhatsappBillingInvoice> listCurrentUserInvoices();

  List<WhatsappBillingInvoice> listAllInvoices();

  WhatsappBillingAccount saveAccount(WhatsappBillingAccount account);

  WhatsappBillingInvoice generateCurrentInvoice(String id);

  void markPaid(String id);

  void toggleSuspension(String id);

  void assertSendingAllowed(WhatsappConnection connection);
}
