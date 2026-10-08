package org.pahappa.systems.egoSms.models.billing;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.sers.webutils.model.BaseEntity;
import org.sers.webutils.model.security.User;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="egosms_service_fee_subscriptions",uniqueConstraints=@UniqueConstraint(columnNames="connection_id"))
public class ServiceFeeSubscription extends BaseEntity {
 private static final long serialVersionUID=1L;
 private User client; private WhatsappConnection connection; private BigDecimal monthlyFee;
 private String currency,status; private Date updatedAt;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="client_user_id",nullable=false)
 public User getClient(){return client;} public void setClient(User v){client=v;}
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="connection_id",nullable=false,unique=true) public WhatsappConnection getConnection(){return connection;} public void setConnection(WhatsappConnection v){connection=v;}
 @Column(name="monthly_fee",nullable=false,precision=19,scale=4) public BigDecimal getMonthlyFee(){return monthlyFee;} public void setMonthlyFee(BigDecimal v){monthlyFee=v;}
 @Column(nullable=false,length=3) public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;}
 @Column(nullable=false,length=24) public String getStatus(){return status;} public void setStatus(String v){status=v;}
 @Temporal(TemporalType.TIMESTAMP) @Column(name="updated_at",nullable=false) public Date getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Date v){updatedAt=v;}
 @Transient public boolean isActive(){return "ACTIVE".equalsIgnoreCase(status);}
}
