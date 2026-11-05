package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.pahappa.systems.egoSms.core.services.whatsapp.*;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WhatsappAutoReplyServiceImpl implements WhatsappAutoReplyService {
  @PersistenceContext private EntityManager entityManager;

  @Autowired private WhatsappAiAssistantService assistantService;

  @Autowired private TokenEncryptionService tokenEncryptionService;

  @Autowired private MetaGraphClient metaGraphClient;

  @Autowired private WhatsappDataService dataService;

  @Override
  public void process(WhatsappMessage inboundMessage) {
    if (inboundMessage == null || inboundMessage.getConversation() == null) {
      return;
    }

    WhatsappMessage managed = entityManager.find(WhatsappMessage.class, inboundMessage.getId());
    if (managed == null || managed.getAiAutoReplyStatus() != null) {
      return;
    }

    WhatsappConnection connection = managed.getConversation().getConnection();
    if (!connection.isAiAutoReplyEnabled()) {
      managed.setAiAutoReplyStatus("DISABLED");
      managed.setStatusDetails("AI auto-reply is disabled for this WhatsApp account.");
      return;
    }

    try {
      WhatsappAiAssistantService.AutoReplyDecision decision =
          assistantService.evaluateAutomaticReply(managed);
      if (!decision.shouldReply()) {
        managed.setAiAutoReplyStatus("HANDOFF");
        managed.setStatusDetails("AI handed this message to an agent: " + decision.getReason());
        return;
      }

      Map<String, Object> payload = new LinkedHashMap<String, Object>();
      payload.put("messaging_product", "whatsapp");
      payload.put("recipient_type", "individual");
      payload.put("to", managed.getConversation().getContactNumber());
      payload.put("type", "text");

      Map<String, Object> text = new LinkedHashMap<String, Object>();
      text.put("body", decision.getMessage());
      payload.put("text", text);

      String token = tokenEncryptionService.decrypt(connection.getAccessTokenCiphertext());
      JsonNode response =
          metaGraphClient.post("/" + connection.getPhoneNumberId() + "/messages", token, payload);
      String metaId = response.path("messages").path(0).path("id").asText(null);
      if (metaId == null || metaId.trim().isEmpty()) {
        throw new IllegalStateException("Meta returned no message ID");
      }

      dataService.saveOutboundEcho(
          connection,
          managed.getConversation().getContactNumber(),
          metaId,
          decision.getMessage(),
          new Date());
      managed.setAiAutoReplyStatus("REPLIED");
      managed.setStatusDetails("AI automatic reply sent.");
    } catch (Exception error) {
      managed.setAiAutoReplyStatus("FAILED");
      String detail = error.getMessage();
      managed.setStatusDetails(
          "AI automatic reply failed"
              + (detail == null || detail.trim().isEmpty() ? "" : ": " + detail));
    }
  }
}
