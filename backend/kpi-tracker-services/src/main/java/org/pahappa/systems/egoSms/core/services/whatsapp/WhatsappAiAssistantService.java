package org.pahappa.systems.egoSms.core.services.whatsapp;

import org.pahappa.systems.egoSms.models.whatsapp.WhatsappMessage;
import org.sers.webutils.model.exception.OperationFailedException;

public interface WhatsappAiAssistantService {
  String suggestReply(String conversationId) throws OperationFailedException;

  String summarize(String conversationId) throws OperationFailedException;

  AutoReplyDecision evaluateAutomaticReply(WhatsappMessage inboundMessage)
      throws OperationFailedException;

  final class AutoReplyDecision {
    private final boolean reply;
    private final String message;
    private final String reason;

    public AutoReplyDecision(boolean reply, String message, String reason) {
      this.reply = reply;
      this.message = message;
      this.reason = reason;
    }

    public boolean shouldReply() {
      return reply;
    }

    public String getMessage() {
      return message;
    }

    public String getReason() {
      return reason;
    }
  }
}
