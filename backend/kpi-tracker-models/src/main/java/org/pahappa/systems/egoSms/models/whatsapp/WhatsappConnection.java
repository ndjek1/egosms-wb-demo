package org.pahappa.systems.egoSms.models.whatsapp;

import org.sers.webutils.model.BaseEntity;
import org.sers.webutils.model.security.User;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="whatsapp_connections", uniqueConstraints={@UniqueConstraint(columnNames="waba_id"), @UniqueConstraint(columnNames="phone_number_id")})
public class WhatsappConnection extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private User owner;
    private String businessId, wabaId, phoneNumberId, displayPhoneNumber, verifiedName, accessTokenCiphertext, connectionStatus;
    private Date updatedAt;
    @Column(name="business_id",length=128) public String getBusinessId(){return businessId;} public void setBusinessId(String v){businessId=v;}
    @Column(name="waba_id",nullable=false,length=128) public String getWabaId(){return wabaId;} public void setWabaId(String v){wabaId=v;}
    @Column(name="phone_number_id",nullable=false,length=128) public String getPhoneNumberId(){return phoneNumberId;} public void setPhoneNumberId(String v){phoneNumberId=v;}
    @Column(name="display_phone_number",length=64) public String getDisplayPhoneNumber(){return displayPhoneNumber;} public void setDisplayPhoneNumber(String v){displayPhoneNumber=v;}
    @Column(name="verified_name") public String getVerifiedName(){return verifiedName;} public void setVerifiedName(String v){verifiedName=v;}
    @Lob @Column(name="access_token_ciphertext",nullable=false) public String getAccessTokenCiphertext(){return accessTokenCiphertext;} public void setAccessTokenCiphertext(String v){accessTokenCiphertext=v;}
    @Column(name="connection_status",nullable=false,length=32) public String getConnectionStatus(){return connectionStatus;} public void setConnectionStatus(String v){connectionStatus=v;}
    @Temporal(TemporalType.TIMESTAMP) @Column(name="updated_at",nullable=false) public Date getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Date v){updatedAt=v;}
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_user_id",nullable=false) public User getOwner(){return owner;} public void setOwner(User v){owner=v;}
}
