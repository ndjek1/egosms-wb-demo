package org.pahappa.systems.egoSms.models.whatsapp;

import org.sers.webutils.model.BaseEntity;
import javax.persistence.*;
import java.util.Date;

@Entity @Table(name="whatsapp_messages",uniqueConstraints=@UniqueConstraint(columnNames="meta_message_id"))
public class WhatsappMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private WhatsappConversation conversation; private WhatsappTemplate template; private WhatsappEnums.Direction direction;
    private String body,metaMessageId,statusDetails; private WhatsappEnums.MessageStatus messageStatus; private Date sentAt;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conversation_id") public WhatsappConversation getConversation(){return conversation;} public void setConversation(WhatsappConversation v){conversation=v;}
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) public WhatsappEnums.Direction getDirection(){return direction;} public void setDirection(WhatsappEnums.Direction v){direction=v;}
    @Lob @Column(nullable=false) public String getBody(){return body;} public void setBody(String v){body=v;}
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="template_id") public WhatsappTemplate getTemplate(){return template;} public void setTemplate(WhatsappTemplate v){template=v;}
    @Column(name="meta_message_id",length=255) public String getMetaMessageId(){return metaMessageId;} public void setMetaMessageId(String v){metaMessageId=v;}
    @Enumerated(EnumType.STRING) @Column(name="message_status",nullable=false,length=16) public WhatsappEnums.MessageStatus getMessageStatus(){return messageStatus;} public void setMessageStatus(WhatsappEnums.MessageStatus v){messageStatus=v;}
    @Lob @Column(name="status_details") public String getStatusDetails(){return statusDetails;} public void setStatusDetails(String v){statusDetails=v;}
    @Temporal(TemporalType.TIMESTAMP) @Column(name="sent_at",nullable=false) public Date getSentAt(){return sentAt;} public void setSentAt(Date v){sentAt=v;}
}
