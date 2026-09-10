package org.pahappa.systems.egoSms.core.webControllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pahappa.systems.egoSms.core.services.whatsapp.*;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;
import org.pahappa.systems.egoSms.core.services.impl.whatsapp.MetaConfiguration;

@Controller
@RequestMapping("/whatsapp/webhooks")
public class WhatsappWebhookController{
 private final ObjectMapper mapper=new ObjectMapper();
 @Autowired private WhatsappConnectionService connectionService;
 @Autowired private WhatsappDataService dataService;
 @Autowired private MetaConfiguration configuration;

 @RequestMapping(method=RequestMethod.GET) @ResponseBody
 public ResponseEntity<String> verify(@RequestParam(value="hub.mode",required=false)String mode,@RequestParam(value="hub.verify_token",required=false)String token,@RequestParam(value="hub.challenge",required=false)String challenge){String expected=configuration.webhookVerifyToken();if("subscribe".equals(mode)&&expected.equals(token)&&challenge!=null)return new ResponseEntity<String>(challenge,HttpStatus.OK);return new ResponseEntity<String>("Verification failed",HttpStatus.FORBIDDEN);}

 @RequestMapping(method=RequestMethod.POST) @ResponseBody
 public ResponseEntity<String> receive(@RequestBody String payload){try{JsonNode root=mapper.readTree(payload);for(JsonNode entry:root.path("entry"))for(JsonNode change:entry.path("changes")){JsonNode value=change.path("value");String phoneId=value.path("metadata").path("phone_number_id").asText(null);if(phoneId==null)continue;WhatsappConnection connection=connectionService.findByPhoneNumberId(phoneId);if(connection==null)continue;for(JsonNode message:value.path("messages")){String from=message.path("from").asText();String id=message.path("id").asText();String body=messageBody(message);String name=value.path("contacts").path(0).path("profile").path("name").asText(null);if(!from.isEmpty()&&!id.isEmpty())dataService.saveInbound(connection,from,name,id,body);}for(JsonNode status:value.path("statuses")){String id=status.path("id").asText();WhatsappEnums.MessageStatus parsed=status(status.path("status").asText());if(!id.isEmpty()&&parsed!=null)dataService.updateMessageStatus(id,parsed,statusDetails(status));}}}catch(Exception ignored){/* Always acknowledge so Meta does not retry malformed or irrelevant events. */}return new ResponseEntity<String>("EVENT_RECEIVED",HttpStatus.OK);}
 private String messageBody(JsonNode m){String type=m.path("type").asText();if("text".equals(type))return m.path("text").path("body").asText("");if("button".equals(type))return m.path("button").path("text").asText("");if("interactive".equals(type)){JsonNode i=m.path("interactive");if(i.has("button_reply"))return i.path("button_reply").path("title").asText("");if(i.has("list_reply"))return i.path("list_reply").path("title").asText("");}return "["+type+" message]";}
 private WhatsappEnums.MessageStatus status(String value){try{return WhatsappEnums.MessageStatus.valueOf(value.toUpperCase(Locale.ENGLISH));}catch(Exception e){return null;}}
 private String statusDetails(JsonNode status){JsonNode errors=status.path("errors");if(!errors.isArray()||errors.size()==0)return null;JsonNode error=errors.get(0);StringBuilder result=new StringBuilder();if(error.has("code"))result.append("Meta ").append(error.path("code").asText()).append(": ");String title=error.path("title").asText("");String message=error.path("message").asText("");String detail=error.path("error_data").path("details").asText("");result.append(!title.isEmpty()?title:message);if(!detail.isEmpty()){if(result.length()>0)result.append(" — ");result.append(detail);}return result.toString();}
}
