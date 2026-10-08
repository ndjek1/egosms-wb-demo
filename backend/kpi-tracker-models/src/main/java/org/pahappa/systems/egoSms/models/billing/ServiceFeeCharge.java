package org.pahappa.systems.egoSms.models.billing;

import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappMessage;
import org.sers.webutils.model.BaseEntity;
import org.sers.webutils.model.security.User;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="egosms_service_fee_charges",uniqueConstraints={@UniqueConstraint(columnNames="message_id"),@UniqueConstraint(columnNames={"connection_id","billing_period","charge_type"})})
public class ServiceFeeCharge extends BaseEntity {
 private static final long serialVersionUID=1L;
 private User client;
 private WhatsappConnection connection;
 private WhatsappMessage message;
 private BigDecimal amount;
 private String currency,chargeType,billingPeriod,status,description;
 private Date chargedAt;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="client_user_id",nullable=false)
 public User getClient(){return client;}
 public void setClient(User v){client=v;}
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="connection_id") public WhatsappConnection getConnection(){return connection;}
 public void setConnection(WhatsappConnection v){connection=v;}
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="message_id",unique=true)
 public WhatsappMessage getMessage(){return message;}
 public void setMessage(WhatsappMessage v){message=v;}
 @Column(nullable=false,precision=19,scale=4)
 public BigDecimal getAmount(){return amount;}
 public void setAmount(BigDecimal v){amount=v;}
 @Column(nullable=false,length=3) public String getCurrency(){return currency;}
 public void setCurrency(String v){currency=v;}
 @Column(name="charge_type",nullable=false,length=40)
 public String getChargeType(){return chargeType;}
 public void setChargeType(String v){chargeType=v;}
 @Column(name="billing_period",length=7)
 public String getBillingPeriod(){return billingPeriod;}
 public void setBillingPeriod(String v){billingPeriod=v;}
 @Column(nullable=false,length=24) public String getStatus(){return status;}
 public void setStatus(String v){status=v;}
 @Column(nullable=false,length=255)
 public String getDescription(){return description;}
 public void setDescription(String v){description=v;}
 @Temporal(TemporalType.TIMESTAMP) @Column(name="charged_at",nullable=false)
 public Date getChargedAt(){return chargedAt;}
 public void setChargedAt(Date v){chargedAt=v;}
}
