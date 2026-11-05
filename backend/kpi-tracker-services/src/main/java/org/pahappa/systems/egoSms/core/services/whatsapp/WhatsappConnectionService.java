package org.pahappa.systems.egoSms.core.services.whatsapp;

import java.util.List;
import org.pahappa.systems.egoSms.core.services.GenericService;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;

public interface WhatsappConnectionService extends GenericService<WhatsappConnection> {
  List<WhatsappConnection> getLoggedInUserConnections();

  List<WhatsappConnection> getLoggedInUserAllConnections();

  WhatsappConnection getLoggedInUserConnection(String id) throws OperationFailedException;

  WhatsappConnection findByPhoneNumberId(String phoneNumberId);

  WhatsappConnection connect(
      String businessId,
      String wabaId,
      String phoneNumberId,
      String encryptedToken,
      String displayNumber,
      String verifiedName)
      throws ValidationFailedException, OperationFailedException;

  void disconnect(String id) throws OperationFailedException;

  void updateAiAutoReply(String id, boolean enabled) throws OperationFailedException;

  void updateAiBusinessContext(String id, String context, String sourceUrl)
      throws OperationFailedException;

  String importAiBusinessContext(String sourceUrl) throws OperationFailedException;
}
