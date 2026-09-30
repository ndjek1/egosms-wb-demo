package org.pahappa.systems.egoSms.core.services.whatsapp;

import org.pahappa.systems.egoSms.models.whatsapp.WhatsappMessage;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappSendRequest;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;

public interface WhatsappSendService {
    WhatsappMessage send(WhatsappSendRequest request) throws ValidationFailedException, OperationFailedException;
    String uploadHeaderMedia(String connectionId, String fileName, String contentType, byte[] content) throws ValidationFailedException, OperationFailedException;
}
