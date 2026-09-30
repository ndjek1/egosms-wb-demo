package org.pahappa.systems.egoSms.core.services.whatsapp;

import org.pahappa.systems.egoSms.models.whatsapp.WhatsappTemplate;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappTemplateRequest;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;
import java.util.List;

public interface WhatsappTemplateService {
    WhatsappTemplate create(WhatsappTemplateRequest request) throws ValidationFailedException, OperationFailedException;
    List<WhatsappTemplate> listAndSync(String connectionId) throws OperationFailedException;
    String uploadSample(String connectionId,String fileName,String contentType,byte[] content) throws ValidationFailedException,OperationFailedException;
}
