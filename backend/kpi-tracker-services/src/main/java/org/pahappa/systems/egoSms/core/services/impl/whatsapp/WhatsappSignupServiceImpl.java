package org.pahappa.systems.egoSms.core.services.impl.whatsapp;
import com.fasterxml.jackson.databind.JsonNode;import org.pahappa.systems.egoSms.core.services.whatsapp.*;import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;import org.sers.webutils.model.exception.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@Service @Transactional public class WhatsappSignupServiceImpl implements WhatsappSignupService{
 @Autowired private MetaGraphClient graph;@Autowired private MetaConfiguration config;@Autowired private TokenEncryptionService encryption;@Autowired private WhatsappConnectionService connections;
 @Override public WhatsappConnection complete(String code,String businessId,String wabaId,String phoneId,boolean coexistence)throws ValidationFailedException,OperationFailedException{
  digits(wabaId,"WABA ID");
  JsonNode tokenResponse=graph.exchangeCode(code);String token=tokenResponse.path("access_token").asText();
  if(token.isEmpty())throw new IllegalStateException("Meta did not return an access token");
  JsonNode phone=resolvePhone(wabaId,phoneId,token,coexistence);phoneId=phone.path("id").asText(phoneId);
  digits(phoneId,"phone number ID");
  graph.post("/"+wabaId+"/subscribed_apps",token,Collections.emptyMap());
  if(!coexistence){Map<String,Object> register=new HashMap<String,Object>();register.put("messaging_product","whatsapp");register.put("pin",config.registrationPin());graph.post("/"+phoneId+"/register",token,register);}
  WhatsappConnection connection=connections.connect(businessId,wabaId,phoneId,encryption.encrypt(token),phone.path("display_phone_number").asText(null),phone.path("verified_name").asText(null));
  if(coexistence)requestCoexistenceSync(phoneId,token);
  return connection;
 }
 private JsonNode resolvePhone(String wabaId,String requestedPhoneId,String token,boolean coexistence){
  if(requestedPhoneId!=null&&!requestedPhoneId.trim().isEmpty())return graph.get("/"+requestedPhoneId+"?fields=id,display_phone_number,verified_name,is_on_biz_app,platform_type",token);
  if(!coexistence)throw new IllegalArgumentException("phone number ID is required");
  JsonNode phones=graph.get("/"+wabaId+"/phone_numbers?fields=id,display_phone_number,verified_name,is_on_biz_app,platform_type",token).path("data");
  JsonNode candidate=null;int count=0;
  for(JsonNode phone:phones)if(phone.path("is_on_biz_app").asBoolean(false)){candidate=phone;count++;}
  if(count==0&&phones.size()==1){candidate=phones.get(0);count=1;}
  if(count!=1)throw new IllegalStateException(count==0?"Meta did not return an eligible WhatsApp Business app phone number for this WABA.":"This WABA contains multiple coexistence numbers. Select one in Meta and try again.");
  return candidate;
 }
 private void requestCoexistenceSync(String phoneId,String token){
  Map<String,Object> request=new HashMap<String,Object>();request.put("messaging_product","whatsapp");
  request.put("sync_type","smb_app_state_sync");try{graph.post("/"+phoneId+"/smb_app_data",token,request);}catch(RuntimeException ignored){}
  request.put("sync_type","history");try{graph.post("/"+phoneId+"/smb_app_data",token,request);}catch(RuntimeException ignored){}
 }
 private void digits(String value,String label){if(value==null||!value.matches("^[0-9]+$"))throw new IllegalArgumentException(label+" must contain digits only");}
}
