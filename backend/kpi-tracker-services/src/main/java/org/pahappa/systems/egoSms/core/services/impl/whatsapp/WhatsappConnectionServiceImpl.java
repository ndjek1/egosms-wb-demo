package org.pahappa.systems.egoSms.core.services.impl.whatsapp;
import com.googlecode.genericdao.search.Search;
import org.pahappa.systems.egoSms.core.services.impl.GenericServiceImpl;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappConnectionService;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;import java.util.List;
@Service @Transactional
public class WhatsappConnectionServiceImpl extends GenericServiceImpl<WhatsappConnection> implements WhatsappConnectionService{
 private User currentUser(){User u=SharedAppData.getLoggedInUser();if(u==null)throw new IllegalStateException("A logged-in user is required");return u;}
 @Override public WhatsappConnection saveInstance(WhatsappConnection c)throws ValidationFailedException,OperationFailedException{
  if(c==null)throw new ValidationFailedException("Missing WhatsApp connection");
  if(c.getWabaId()==null||c.getWabaId().trim().isEmpty())throw new ValidationFailedException("Missing WABA ID");
  if(c.getPhoneNumberId()==null||c.getPhoneNumberId().trim().isEmpty())throw new ValidationFailedException("Missing phone number ID");
  if(c.getOwner()==null)c.setOwner(currentUser());
  if(!c.getOwner().getId().equals(currentUser().getId()))throw new OperationFailedException("You cannot modify another user's WhatsApp connection");
  c.setUpdatedAt(new Date());
  if(c.getRecordStatus()==null)
   c.setRecordStatus(RecordStatus.ACTIVE);
  if(c.getConnectionStatus()==null)
   c.setConnectionStatus("CONNECTED");
  WhatsappConnection saved=save(c);
  entityManager.flush();
  return saved;}
 @Override public List<WhatsappConnection> getLoggedInUserConnections(){List<WhatsappConnection> stored=search(new Search().addFilterEqual("owner.id",currentUser().getId()).addSortDesc("dateChanged"));List<WhatsappConnection> visible=new java.util.ArrayList<WhatsappConnection>();for(WhatsappConnection c:stored){if(c.getRecordStatus()==null)c.setRecordStatus(RecordStatus.ACTIVE);if(c.getRecordStatus()==RecordStatus.ACTIVE)visible.add(c);}return visible;}
 @Override public WhatsappConnection getLoggedInUserConnection(String id)throws OperationFailedException{WhatsappConnection c=searchUnique(new Search().addFilterEqual("id",id).addFilterEqual("owner.id",currentUser().getId()));if(c!=null&&c.getRecordStatus()==null)c.setRecordStatus(RecordStatus.ACTIVE);if(c==null||c.getRecordStatus()!=RecordStatus.ACTIVE)throw new OperationFailedException("WhatsApp connection was not found");return c;}
 @Override public WhatsappConnection findByPhoneNumberId(String id){WhatsappConnection c=searchUnique(new Search().addFilterEqual("phoneNumberId",id));if(c!=null&&c.getRecordStatus()==null)c.setRecordStatus(RecordStatus.ACTIVE);return c!=null&&c.getRecordStatus()==RecordStatus.ACTIVE?c:null;}
 @Override public WhatsappConnection connect(String b,String w,String p,String token,String display,String name)throws ValidationFailedException,OperationFailedException{WhatsappConnection c=searchUnique(new Search().addFilterEqual("owner.id",currentUser().getId()).addFilterEqual("wabaId",w));if(c==null)c=new WhatsappConnection();c.setOwner(currentUser());c.setBusinessId(b);c.setWabaId(w);c.setPhoneNumberId(p);c.setAccessTokenCiphertext(token);c.setDisplayPhoneNumber(display);c.setVerifiedName(name);c.setConnectionStatus("CONNECTED");return saveInstance(c);}
 @Override public boolean isDeletable(WhatsappConnection c){return c!=null&&c.getOwner()!=null&&c.getOwner().getId().equals(currentUser().getId());}
}
