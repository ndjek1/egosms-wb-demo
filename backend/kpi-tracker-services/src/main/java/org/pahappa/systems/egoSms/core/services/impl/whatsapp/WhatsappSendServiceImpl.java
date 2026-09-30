package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappConnectionService;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappDataService;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappSendService;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Service
@Transactional
public class WhatsappSendServiceImpl implements WhatsappSendService {
    @PersistenceContext private EntityManager entityManager;
    @Autowired private WhatsappConnectionService connectionService;
    @Autowired private WhatsappDataService dataService;
    @Autowired private TokenEncryptionService tokenEncryptionService;
    @Autowired private MetaGraphClient metaGraphClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String uploadHeaderMedia(String connectionId,String fileName,String contentType,byte[] content) throws ValidationFailedException,OperationFailedException {
        require(connectionId,"Select a WhatsApp account");
        if(content==null||content.length==0)throw new ValidationFailedException("Choose a non-empty media file");
        if(content.length>16*1024*1024)throw new ValidationFailedException("Header media must not exceed 16 MB");
        if(blank(contentType)||!(contentType.startsWith("image/")||"video/mp4".equals(contentType)||"application/pdf".equals(contentType)))throw new ValidationFailedException("Choose an image, MP4 video, or PDF document");
        WhatsappConnection connection=connectionService.getLoggedInUserConnection(connectionId);
        JsonNode response=metaGraphClient.uploadMedia("/"+connection.getPhoneNumberId()+"/media",tokenEncryptionService.decrypt(connection.getAccessTokenCiphertext()),blank(fileName)?"header-media":fileName,contentType,content);
        String id=response.path("id").asText(null);if(blank(id))throw new IllegalStateException("Meta uploaded the file without returning a media ID");return id;
    }

    @Override
    public WhatsappMessage send(WhatsappSendRequest request) throws ValidationFailedException, OperationFailedException {
        if (request == null) throw new ValidationFailedException("Message details are required");
        require(request.getConnectionId(), "Select a WhatsApp account");
        require(request.getRecipientNumber(), "Recipient number is required");
        WhatsappConnection connection = connectionService.getLoggedInUserConnection(request.getConnectionId());
        String recipient = normalize(request.getRecipientNumber());
        WhatsappTemplate template = null;
        Map<String,Object> payload;
        String storedBody;
        if (!blank(request.getTemplateId())) {
            template = ownedTemplate(connection, request.getTemplateId());
            payload = templatePayload(recipient, template, request);
            storedBody = render(template.getBody(), request.getBodyVariables());
        } else {
            require(request.getRawBody(), "Enter a message or select a template");
            WhatsappConversation existing = findConversation(connection, recipient);
            if (existing == null || !existing.isWindowOpen()) throw new ValidationFailedException("Free-form replies are only allowed within 24 hours of the customer's last message. Use an approved template.");
            payload = base(recipient); payload.put("type", "text"); payload.put("text", singleton("body", request.getRawBody().trim()));
            storedBody = request.getRawBody().trim();
        }
        JsonNode response = metaGraphClient.post("/" + connection.getPhoneNumberId() + "/messages", tokenEncryptionService.decrypt(connection.getAccessTokenCiphertext()), payload);
        String metaMessageId = response.path("messages").path(0).path("id").asText(null);
        if (blank(metaMessageId)) throw new IllegalStateException("Meta accepted the request without returning a message ID");
        WhatsappConversation conversation = dataService.findOrCreateConversation(connection, recipient, null);
        WhatsappMessage message = new WhatsappMessage();
        message.setConversation(conversation); message.setTemplate(template); message.setDirection(WhatsappEnums.Direction.OUTBOUND);
        message.setBody(storedBody); message.setMetaMessageId(metaMessageId); message.setMessageStatus(WhatsappEnums.MessageStatus.SENT);
        message.setSentAt(new Date()); message.setRecordStatus(RecordStatus.ACTIVE); entityManager.persist(message);
        return message;
    }

    private Map<String,Object> templatePayload(String recipient, WhatsappTemplate template, WhatsappSendRequest request) throws ValidationFailedException {
        if (template.getApprovalStatus() != WhatsappEnums.ApprovalStatus.APPROVED) throw new ValidationFailedException("Only approved templates can be sent");
        Map<String,Object> payload = base(recipient); payload.put("type", "template");
        Map<String,Object> definition = new LinkedHashMap<String,Object>(); definition.put("name", template.getName()); definition.put("language", singleton("code", blank(template.getLanguage()) ? "en_US" : template.getLanguage()));
        List<Map<String,Object>> values = new ArrayList<Map<String,Object>>();
        if (template.getCategory() == WhatsappEnums.TemplateCategory.AUTHENTICATION) {
            if (request.getBodyVariables() == null || request.getBodyVariables().size() != 1) throw new ValidationFailedException("Enter the one-time password for this authentication message");
            String otp = request.getBodyVariables().get(0);
            require(otp, "One-time password is required");
            Map<String,Object> body = component("body");
            body.put("parameters", Collections.singletonList(textParameter(otp.trim())));
            values.add(body);
            Map<String,Object> button = component("button");
            button.put("sub_type", "url");
            button.put("index", "0");
            button.put("parameters", Collections.singletonList(textParameter(otp.trim())));
            values.add(button);
            definition.put("components", values);
            payload.put("template", definition);
            return payload;
        }
        JsonNode stored;
        try { stored = mapper.readTree(template.getComponentsJson() == null ? "[]" : template.getComponentsJson()); }
        catch (Exception e) { throw new IllegalStateException("Stored template components are invalid", e); }
        if (stored.isArray()) for (JsonNode component : stored) {
            String type = component.path("type").asText("").toUpperCase(Locale.ENGLISH);
            if ("BODY".equals(type) && component.path("text").asText("").contains("{{")) {
                Map<String,Object> body = component("body"); body.put("parameters", textParameters(request.getBodyVariables())); values.add(body);
            } else if ("HEADER".equals(type)) {
                String format = component.path("format").asText("").toLowerCase(Locale.ENGLISH);
                if ("text".equals(format) && component.path("text").asText("").contains("{{")) {
                    require(request.getHeaderVariable(), "A value is required for the header placeholder");
                    Map<String,Object> header = component("header");
                    header.put("parameters", Collections.singletonList(textParameter(request.getHeaderVariable().trim())));
                    values.add(header);
                } else if (Arrays.asList("image","video","document").contains(format)) {
                    require(request.getHeaderMediaId(), "Select/upload header media before sending this template");
                    Map<String,Object> media = singleton("type", format); media.put(format, singleton("id", request.getHeaderMediaId()));
                    Map<String,Object> header = component("header"); header.put("parameters", Collections.singletonList(media)); values.add(header);
                }
            } else if ("BUTTONS".equals(type)) {
                int variableIndex = 0;
                JsonNode buttons = component.path("buttons");
                if (buttons.isArray()) for (int i=0;i<buttons.size();i++) {
                    JsonNode button = buttons.get(i);
                    if ("URL".equalsIgnoreCase(button.path("type").asText()) && button.path("url").asText("").contains("{{1}}")) {
                        if (variableIndex >= request.getButtonVariables().size()) throw new ValidationFailedException("A value is required for dynamic URL button " + (i+1));
                        Map<String,Object> value = component("button"); value.put("sub_type", "url"); value.put("index", String.valueOf(i));
                        value.put("parameters", Collections.singletonList(textParameter(request.getButtonVariables().get(variableIndex++)))); values.add(value);
                    }
                }
            }
        }
        if (!values.isEmpty()) definition.put("components", values);
        payload.put("template", definition); return payload;
    }

    private WhatsappTemplate ownedTemplate(WhatsappConnection c,String id)throws OperationFailedException {List<WhatsappTemplate> rows=entityManager.createQuery("select t from WhatsappTemplate t where t.id=:id and t.connection=:connection and t.recordStatus=:status",WhatsappTemplate.class).setParameter("id",id).setParameter("connection",c).setParameter("status",RecordStatus.ACTIVE).getResultList();if(rows.isEmpty())throw new OperationFailedException("Template was not found");return rows.get(0);}
    private WhatsappConversation findConversation(WhatsappConnection c,String number){List<WhatsappConversation> rows=entityManager.createQuery("select c from WhatsappConversation c where c.connection=:connection and c.contactNumber=:number and c.recordStatus=:status",WhatsappConversation.class).setParameter("connection",c).setParameter("number",number).setParameter("status",RecordStatus.ACTIVE).getResultList();return rows.isEmpty()?null:rows.get(0);}
    private Map<String,Object> base(String recipient){Map<String,Object> p=new LinkedHashMap<String,Object>();p.put("messaging_product","whatsapp");p.put("recipient_type","individual");p.put("to",recipient);return p;}
    private Map<String,Object> component(String type){Map<String,Object> v=new LinkedHashMap<String,Object>();v.put("type",type);return v;}
    private Map<String,Object> singleton(String key,Object value){Map<String,Object> v=new LinkedHashMap<String,Object>();v.put(key,value);return v;}
    private Map<String,Object> textParameter(String text){Map<String,Object> v=component("text");v.put("text",text==null?"":text);return v;}
    private List<Map<String,Object>> textParameters(List<String> values){List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();for(String value:values)result.add(textParameter(value));return result;}
    private String render(String body,List<String> values){String rendered=body==null?"":body;for(int i=0;i<values.size();i++)rendered=rendered.replace("{{"+(i+1)+"}}",values.get(i)==null?"":values.get(i));return rendered;}
    private String normalize(String value){return value.trim().replaceAll("[^0-9]","");}
    private void require(String value,String message)throws ValidationFailedException{if(blank(value))throw new ValidationFailedException(message);}
    private boolean blank(String value){return value==null||value.trim().isEmpty();}
}
