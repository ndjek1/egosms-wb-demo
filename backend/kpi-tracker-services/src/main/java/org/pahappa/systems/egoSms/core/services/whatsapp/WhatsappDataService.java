package org.pahappa.systems.egoSms.core.services.whatsapp;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.sers.webutils.model.exception.OperationFailedException;
import java.util.List;
import java.util.Map;
public interface WhatsappDataService{
 List<WhatsappTemplate> getTemplates(String connectionId)throws OperationFailedException;
 List<WhatsappConversation> getConversations(String connectionId)throws OperationFailedException;
 List<WhatsappMessage> getMessages(String conversationId)throws OperationFailedException;
 WhatsappMessage getLatestInboundMessage(String connectionId)throws OperationFailedException;
 Map<String,Long> getUnreadCounts(String connectionId)throws OperationFailedException;
 void markConversationRead(String conversationId)throws OperationFailedException;
 WhatsappConversation findOrCreateConversation(WhatsappConnection connection,String number,String name);
 WhatsappMessage saveInbound(WhatsappConnection connection,String number,String name,String metaMessageId,String body);
 void updateMessageStatus(String metaMessageId,WhatsappEnums.MessageStatus status,String details);
}
