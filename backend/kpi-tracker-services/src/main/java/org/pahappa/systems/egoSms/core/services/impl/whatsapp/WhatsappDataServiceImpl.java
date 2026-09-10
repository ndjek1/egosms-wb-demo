package org.pahappa.systems.egoSms.core.services.impl.whatsapp;
import org.pahappa.systems.egoSms.core.services.whatsapp.*;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.util.*;

@Service @Transactional
public class WhatsappDataServiceImpl implements WhatsappDataService{
 @PersistenceContext private EntityManager entityManager;
 private User user(){User u=SharedAppData.getLoggedInUser();if(u==null)throw new IllegalStateException("A logged-in user is required");return u;}
 private WhatsappConnection ownedConnection(String id)throws OperationFailedException{List<WhatsappConnection> rows=entityManager.createQuery("select c from WhatsappConnection c where c.id=:id and c.owner.id=:owner and c.recordStatus=:status",WhatsappConnection.class).setParameter("id",id).setParameter("owner",user().getId()).setParameter("status",RecordStatus.ACTIVE).getResultList();if(rows.isEmpty())throw new OperationFailedException("WhatsApp connection was not found");return rows.get(0);}
 @Override @Transactional(readOnly=true) public List<WhatsappTemplate> getTemplates(String connectionId)throws OperationFailedException{WhatsappConnection c=ownedConnection(connectionId);return entityManager.createQuery("select t from WhatsappTemplate t where t.connection=:connection and t.recordStatus=:status order by t.dateCreated desc",WhatsappTemplate.class).setParameter("connection",c).setParameter("status",RecordStatus.ACTIVE).getResultList();}
 @Override @Transactional(readOnly=true) public List<WhatsappConversation> getConversations(String connectionId)throws OperationFailedException{WhatsappConnection c=ownedConnection(connectionId);return entityManager.createQuery("select c from WhatsappConversation c where c.connection=:connection and c.recordStatus=:status order by c.lastActivityAt desc",WhatsappConversation.class).setParameter("connection",c).setParameter("status",RecordStatus.ACTIVE).getResultList();}
 @Override @Transactional(readOnly=true) public List<WhatsappMessage> getMessages(String conversationId)throws OperationFailedException{List<WhatsappConversation> rows=entityManager.createQuery("select c from WhatsappConversation c where c.id=:id and c.connection.owner.id=:owner and c.recordStatus=:status",WhatsappConversation.class).setParameter("id",conversationId).setParameter("owner",user().getId()).setParameter("status",RecordStatus.ACTIVE).getResultList();if(rows.isEmpty())throw new OperationFailedException("Conversation was not found");return entityManager.createQuery("select m from WhatsappMessage m where m.conversation=:conversation and m.recordStatus=:status order by m.sentAt",WhatsappMessage.class).setParameter("conversation",rows.get(0)).setParameter("status",RecordStatus.ACTIVE).getResultList();}
 @Override public WhatsappConversation findOrCreateConversation(WhatsappConnection connection,String number,String name){List<WhatsappConversation> rows=entityManager.createQuery("select c from WhatsappConversation c where c.connection=:connection and c.contactNumber=:number",WhatsappConversation.class).setParameter("connection",connection).setParameter("number",number).getResultList();WhatsappConversation c=rows.isEmpty()?new WhatsappConversation():rows.get(0);if(rows.isEmpty()){c.setConnection(connection);c.setContactNumber(number);c.setRecordStatus(RecordStatus.ACTIVE);}if(name!=null&&!name.trim().isEmpty())c.setContactName(name.trim());c.setLastActivityAt(new Date());return rows.isEmpty()?persist(c):c;}
 @Override public WhatsappMessage saveInbound(WhatsappConnection connection,String number,String name,String metaId,String body)
 {List<WhatsappMessage> duplicate=entityManager.createQuery("select m from WhatsappMessage m where m.metaMessageId=:id",WhatsappMessage.class).setParameter("id",metaId).getResultList();if(!duplicate.isEmpty())return duplicate.get(0);WhatsappConversation c=findOrCreateConversation(connection,number,name);Calendar expiry=Calendar.getInstance();expiry.add(Calendar.HOUR,24);c.setWindowExpiresAt(expiry.getTime());WhatsappMessage m=new WhatsappMessage();m.setConversation(c);m.setDirection(WhatsappEnums.Direction.INBOUND);m.setBody(body==null?"":body);m.setMetaMessageId(metaId);m.setMessageStatus(WhatsappEnums.MessageStatus.DELIVERED);m.setSentAt(new Date());m.setRecordStatus(RecordStatus.ACTIVE);return persist(m);}
 @Override public void updateMessageStatus(String id,WhatsappEnums.MessageStatus status,String details){List<WhatsappMessage> rows=entityManager.createQuery("select m from WhatsappMessage m where m.metaMessageId=:id",WhatsappMessage.class).setParameter("id",id).getResultList();if(!rows.isEmpty()){rows.get(0).setMessageStatus(status);rows.get(0).setStatusDetails(details);}}
 private <T>T persist(T value){entityManager.persist(value);return value;}
}
