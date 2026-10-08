package org.pahappa.systems.egoSms.core.services.impl.billing;
import org.pahappa.systems.egoSms.core.services.billing.ServiceFeeBillingService;
import org.pahappa.systems.egoSms.core.services.impl.whatsapp.MetaConfiguration;
import org.pahappa.systems.egoSms.models.billing.ServiceFeeCharge;
import org.pahappa.systems.egoSms.models.billing.ServiceFeeSubscription;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Service @Transactional
public class ServiceFeeBillingServiceImpl implements ServiceFeeBillingService {
 @PersistenceContext private EntityManager entityManager;
 @Autowired private MetaConfiguration configuration;
 @Override public void ensureCurrentMonthlySubscription(WhatsappConnection connection){
  if(!isEnabled()||connection==null)return;
  ServiceFeeSubscription subscription=findOrCreateSubscription(connection);
  if(!subscription.isActive()||subscription.getMonthlyFee()==null||subscription.getMonthlyFee().compareTo(BigDecimal.ZERO)<=0)return;
  User client=connection.getOwner();String period=currentPeriod();
  Long existing=entityManager.createQuery("select count(c) from ServiceFeeCharge c where c.connection.id=:connectionId and c.billingPeriod=:period and c.chargeType=:chargeType",Long.class).setParameter("connectionId",connection.getId()).setParameter("period",period).setParameter("chargeType","MONTHLY_SUBSCRIPTION").getSingleResult();
  if(existing!=null&&existing>0)return;
  ServiceFeeCharge charge=new ServiceFeeCharge();
  charge.setClient(client);charge.setConnection(connection);charge.setAmount(subscription.getMonthlyFee());charge.setCurrency(subscription.getCurrency());charge.setChargeType("MONTHLY_SUBSCRIPTION");charge.setBillingPeriod(period);charge.setStatus("POSTED");
  charge.setDescription("egoSms monthly subscription for "+accountLabel(connection)+" — "+period);charge.setChargedAt(new Date());charge.setRecordStatus(RecordStatus.ACTIVE);entityManager.persist(charge);
 }
 @Override public void ensureLoggedInClientMonthlySubscriptions(){for(WhatsappConnection connection:entityManager.createQuery("select c from WhatsappConnection c where c.owner.id=:ownerId and c.connectionStatus=:status and c.recordStatus=:recordStatus",WhatsappConnection.class).setParameter("ownerId",currentUser().getId()).setParameter("status","CONNECTED").setParameter("recordStatus",RecordStatus.ACTIVE).getResultList())ensureCurrentMonthlySubscription(connection);}
 @Override @Transactional(readOnly=true) public List<ServiceFeeCharge> getLoggedInClientCharges(){return entityManager.createQuery("select c from ServiceFeeCharge c join fetch c.connection where c.client.id=:clientId and c.chargeType=:chargeType and c.recordStatus=:recordStatus order by c.chargedAt desc",ServiceFeeCharge.class).setParameter("clientId",currentUser().getId()).setParameter("chargeType","MONTHLY_SUBSCRIPTION").setParameter("recordStatus",RecordStatus.ACTIVE).getResultList();}
 @Override @Transactional(readOnly=true) public BigDecimal getLoggedInClientTotal(){BigDecimal total=entityManager.createQuery("select sum(c.amount) from ServiceFeeCharge c where c.client.id=:clientId and c.chargeType=:chargeType and c.status=:status and c.recordStatus=:recordStatus",BigDecimal.class).setParameter("clientId",currentUser().getId()).setParameter("chargeType","MONTHLY_SUBSCRIPTION").setParameter("status","POSTED").setParameter("recordStatus",RecordStatus.ACTIVE).getSingleResult();return total==null?BigDecimal.ZERO:total;}
 @Override @Transactional(readOnly=true) public long getLoggedInClientBillingPeriodCount(){Long count=entityManager.createQuery("select count(c) from ServiceFeeCharge c where c.client.id=:clientId and c.chargeType=:chargeType and c.status=:status and c.recordStatus=:recordStatus",Long.class).setParameter("clientId",currentUser().getId()).setParameter("chargeType","MONTHLY_SUBSCRIPTION").setParameter("status","POSTED").setParameter("recordStatus",RecordStatus.ACTIVE).getSingleResult();return count==null?0:count;}
 @Override public BigDecimal getMonthlySubscriptionFee(){return configuration.monthlySubscriptionFee();}
 @Override public String getCurrency(){return configuration.serviceFeeCurrency();}
 @Override public boolean isEnabled(){return configuration.serviceFeeBillingEnabled();}
 @Override public List<ServiceFeeSubscription> getAllSubscriptionsForAdmin(){requireAdministrator();List<WhatsappConnection> connections=entityManager.createQuery("select c from WhatsappConnection c where c.recordStatus=:recordStatus order by c.dateCreated desc",WhatsappConnection.class).setParameter("recordStatus",RecordStatus.ACTIVE).getResultList();for(WhatsappConnection connection:connections)findOrCreateSubscription(connection);entityManager.flush();return entityManager.createQuery("select s from ServiceFeeSubscription s join fetch s.client join fetch s.connection where s.recordStatus=:recordStatus order by s.updatedAt desc",ServiceFeeSubscription.class).setParameter("recordStatus",RecordStatus.ACTIVE).getResultList();}
 @Override public ServiceFeeSubscription saveSubscriptionForAdmin(ServiceFeeSubscription submitted){requireAdministrator();if(submitted==null||submitted.getId()==null)throw new IllegalArgumentException("Billing account is required");ServiceFeeSubscription stored=entityManager.find(ServiceFeeSubscription.class,submitted.getId());if(stored==null)throw new IllegalArgumentException("Billing account was not found");if(submitted.getMonthlyFee()==null||submitted.getMonthlyFee().compareTo(BigDecimal.ZERO)<0)throw new IllegalArgumentException("Monthly fee cannot be negative");String currency=submitted.getCurrency()==null?"":submitted.getCurrency().trim().toUpperCase(Locale.ENGLISH);if(currency.length()!=3)throw new IllegalArgumentException("Currency must be a 3-letter code such as UGX or USD");stored.setMonthlyFee(submitted.getMonthlyFee());stored.setCurrency(currency);stored.setStatus("SUSPENDED".equalsIgnoreCase(submitted.getStatus())?"SUSPENDED":"ACTIVE");stored.setUpdatedAt(new Date());entityManager.flush();return stored;}
 @Override @Transactional(readOnly=true) public BigDecimal getLoggedInClientMonthlyRateTotal(){BigDecimal total=entityManager.createQuery("select sum(s.monthlyFee) from ServiceFeeSubscription s where s.client.id=:clientId and s.status=:status and s.recordStatus=:recordStatus",BigDecimal.class).setParameter("clientId",currentUser().getId()).setParameter("status","ACTIVE").setParameter("recordStatus",RecordStatus.ACTIVE).getSingleResult();return total==null?BigDecimal.ZERO:total;}
 private ServiceFeeSubscription findOrCreateSubscription(WhatsappConnection connection){List<ServiceFeeSubscription> rows=entityManager.createQuery("select s from ServiceFeeSubscription s where s.connection=:connection",ServiceFeeSubscription.class).setParameter("connection",connection).getResultList();if(!rows.isEmpty())return rows.get(0);ServiceFeeSubscription subscription=new ServiceFeeSubscription();subscription.setClient(connection.getOwner());subscription.setConnection(connection);subscription.setMonthlyFee(getMonthlySubscriptionFee());subscription.setCurrency(getCurrency());subscription.setStatus("ACTIVE");subscription.setUpdatedAt(new Date());subscription.setRecordStatus(RecordStatus.ACTIVE);entityManager.persist(subscription);return subscription;}
 private void requireAdministrator(){if(!currentUser().hasAdministrativePrivileges())throw new SecurityException("Administrator privileges are required to manage billing");}
 private String accountLabel(WhatsappConnection connection){if(connection.getVerifiedName()!=null&&!connection.getVerifiedName().trim().isEmpty())return connection.getVerifiedName();if(connection.getDisplayPhoneNumber()!=null&&!connection.getDisplayPhoneNumber().trim().isEmpty())return connection.getDisplayPhoneNumber();return connection.getPhoneNumberId();}
 private String currentPeriod(){return new java.text.SimpleDateFormat("yyyy-MM").format(new Date());}
 private User currentUser(){User user=SharedAppData.getLoggedInUser();if(user==null)throw new IllegalStateException("A logged-in user is required");return user;}
}
