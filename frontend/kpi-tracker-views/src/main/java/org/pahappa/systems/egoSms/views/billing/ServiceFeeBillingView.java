package org.pahappa.systems.egoSms.views.billing;
import org.pahappa.systems.egoSms.core.services.billing.ServiceFeeBillingService;
import org.pahappa.systems.egoSms.models.billing.ServiceFeeCharge;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;
import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@ManagedBean(name="serviceFeeBilling") @ViewScoped
public class ServiceFeeBillingView implements Serializable {
 private static final long serialVersionUID=1L;
 private transient ServiceFeeBillingService billingService;
 private List<ServiceFeeCharge> charges=new ArrayList<ServiceFeeCharge>();
 private BigDecimal total=BigDecimal.ZERO; private long chargeCount;
 @PostConstruct public void init(){refresh();}
 public void refresh(){if(billingService==null)billingService=ApplicationContextProvider.getBean(ServiceFeeBillingService.class);billingService.ensureLoggedInClientMonthlySubscriptions();charges=billingService.getLoggedInClientCharges();total=billingService.getLoggedInClientTotal();chargeCount=billingService.getLoggedInClientBillingPeriodCount();}
 public List<ServiceFeeCharge> getCharges(){return charges;} public BigDecimal getTotal(){return total;} public long getChargeCount(){return chargeCount;}
 public BigDecimal getMonthlyFee(){return billingService.getLoggedInClientMonthlyRateTotal();} public String getCurrency(){return billingService.getCurrency();} public boolean isEnabled(){return billingService.isEnabled();}
}
