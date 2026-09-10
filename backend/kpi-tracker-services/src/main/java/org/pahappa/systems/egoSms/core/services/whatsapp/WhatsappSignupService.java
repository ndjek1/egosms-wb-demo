package org.pahappa.systems.egoSms.core.services.whatsapp;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;import org.sers.webutils.model.exception.*;
public interface WhatsappSignupService{WhatsappConnection complete(String code,String businessId,String wabaId,String phoneNumberId)throws ValidationFailedException,OperationFailedException;}
