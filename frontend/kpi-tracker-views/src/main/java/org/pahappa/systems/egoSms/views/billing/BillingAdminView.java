package org.pahappa.systems.egoSms.views.billing;
import org.pahappa.systems.egoSms.core.services.billing.ServiceFeeBillingService;
import org.pahappa.systems.egoSms.models.billing.ServiceFeeSubscription;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;
import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name="billingAdmin") @ViewScoped
public class BillingAdminView implements Serializable {
 private static final long serialVersionUID=1L;
 private transient ServiceFeeBillingService billingService;
 private List<ServiceFeeSubscription> subscriptions=new ArrayList<ServiceFeeSubscription>();
 @PostConstruct public void init(){refresh();}
 public void refresh(){try{wire();subscriptions=billingService.getAllSubscriptionsForAdmin();}catch(Exception e){fail(e);}}
 public void save(ServiceFeeSubscription subscription){try{wire();billingService.saveSubscriptionForAdmin(subscription);subscriptions=billingService.getAllSubscriptionsForAdmin();UiUtils.showMessageBox("Billing updated","The monthly subscription for this WhatsApp account was saved.");}catch(Exception e){fail(e);}}
 private void wire(){if(billingService==null)billingService=ApplicationContextProvider.getBean(ServiceFeeBillingService.class);}
 private void fail(Exception e){Throwable value=e;while(value.getCause()!=null)value=value.getCause();UiUtils.ComposeFailure("Billing action failed",value.getMessage()==null?value.toString():value.getMessage());}
 public List<ServiceFeeSubscription> getSubscriptions(){return subscriptions;}
 public boolean isBillingEnabled(){wire();return billingService.isEnabled();}
}
