package org.pahappa.systems.egoSms.core.services.whatsapp;

import org.pahappa.systems.egoSms.models.whatsapp.WhatsappMessage;

public interface WhatsappAutoReplyService {
  void process(WhatsappMessage inboundMessage);
}
