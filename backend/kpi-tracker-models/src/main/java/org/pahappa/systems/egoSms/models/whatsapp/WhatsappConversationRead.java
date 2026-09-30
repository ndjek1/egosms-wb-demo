package org.pahappa.systems.egoSms.models.whatsapp;

import org.sers.webutils.model.BaseEntity;
import org.sers.webutils.model.security.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(
        name="whatsapp_conversation_reads",
        uniqueConstraints=@UniqueConstraint(columnNames={"conversation_id","user_id"})
)
public class WhatsappConversationRead extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private WhatsappConversation conversation;
    private User user;
    private Date lastReadAt;

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="conversation_id",nullable=false)
    public WhatsappConversation getConversation(){return conversation;}
    public void setConversation(WhatsappConversation value){conversation=value;}

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="user_id",nullable=false)
    public User getUser(){return user;}
    public void setUser(User value){user=value;}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_read_at",nullable=false)
    public Date getLastReadAt(){return lastReadAt;}
    public void setLastReadAt(Date value){lastReadAt=value;}
}
