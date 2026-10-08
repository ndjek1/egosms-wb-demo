package org.pahappa.systems.egoSms.core.services.billing;
import org.pahappa.systems.egoSms.models.billing.ServiceFeeCharge;
import org.pahappa.systems.egoSms.models.billing.ServiceFeeSubscription;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import java.math.BigDecimal;
import java.util.List;
public interface ServiceFeeBillingService {
 void ensureCurrentMonthlySubscription(WhatsappConnection connection);
 void ensureLoggedInClientMonthlySubscriptions();
 List<ServiceFeeCharge> getLoggedInClientCharges();
 BigDecimal getLoggedInClientTotal();
 long getLoggedInClientBillingPeriodCount();
 BigDecimal getMonthlySubscriptionFee();
 String getCurrency();
 boolean isEnabled();
 List<ServiceFeeSubscription> getAllSubscriptionsForAdmin();
 ServiceFeeSubscription saveSubscriptionForAdmin(ServiceFeeSubscription subscription);
 BigDecimal getLoggedInClientMonthlyRateTotal();
}
