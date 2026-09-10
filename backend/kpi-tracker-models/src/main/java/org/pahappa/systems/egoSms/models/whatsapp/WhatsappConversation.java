package org.pahappa.systems.egoSms.models.whatsapp;

import org.sers.webutils.model.BaseEntity;
import javax.persistence.*;
import java.util.Date;

@Entity @Table(name="whatsapp_conversations", uniqueConstraints={@UniqueConstraint(columnNames={"connection_id","contact_number"})})
public class WhatsappConversation extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private WhatsappConnection connection;
    private String contactNumber,contactName;
    private Date lastActivityAt,windowExpiresAt;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="connection_id") public WhatsappConnection getConnection(){return connection;} public void setConnection(WhatsappConnection v){connection=v;}
    @Column(name="contact_number",nullable=false,length=32) public String getContactNumber(){return contactNumber;} public void setContactNumber(String v){contactNumber=v;}
    @Column(name="contact_name") public String getContactName(){return contactName;} public void setContactName(String v){contactName=v;}
    @Temporal(TemporalType.TIMESTAMP) @Column(name="last_activity_at",nullable=false) public Date getLastActivityAt(){return lastActivityAt;} public void setLastActivityAt(Date v){lastActivityAt=v;}
    @Temporal(TemporalType.TIMESTAMP) @Column(name="window_expires_at") public Date getWindowExpiresAt(){return windowExpiresAt;} public void setWindowExpiresAt(Date v){windowExpiresAt=v;}
    @Transient public boolean isWindowOpen(){return windowExpiresAt!=null&&windowExpiresAt.after(new Date());}
}
