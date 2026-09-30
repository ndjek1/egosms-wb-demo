package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappConnectionService;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappTemplateService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;

@Service
@Transactional
public class WhatsappTemplateServiceImpl implements WhatsappTemplateService {
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{(\\d+)\\}\\}");
    @PersistenceContext private EntityManager entityManager;
    @Autowired private WhatsappConnectionService connectionService;
    @Autowired private TokenEncryptionService tokenEncryptionService;
    @Autowired private MetaGraphClient metaGraphClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public WhatsappTemplate create(WhatsappTemplateRequest request) throws ValidationFailedException, OperationFailedException {
        validate(request);
        WhatsappConnection connection = connectionService.getLoggedInUserConnection(request.getConnectionId());
        List<Map<String,Object>> components = buildComponents(request);
        Map<String,Object> payload = new LinkedHashMap<String,Object>();
        payload.put("name", request.getName().trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_]", "_"));
        payload.put("language", blank(request.getLanguage()) ? "en_US" : request.getLanguage());
        payload.put("category", request.getCategory().name());
        payload.put("components", components);
        JsonNode result = metaGraphClient.post("/" + connection.getWabaId() + "/message_templates", token(connection), payload);

        WhatsappTemplate template = find(connection, String.valueOf(payload.get("name")));
        if (template == null) template = new WhatsappTemplate();
        template.setConnection(connection);
        template.setName(String.valueOf(payload.get("name")));
        template.setLanguage(String.valueOf(payload.get("language")));
        template.setCategory(request.getCategory());
        template.setBody(authentication(request) ? authenticationBody() : request.getBody().trim());
        template.setMetaTemplateId(text(result, "id"));
        template.setApprovalStatus(status(text(result, "status")));
        template.setComponentsJson(json(components));
        template.setRecordStatus(RecordStatus.ACTIVE);
        if (template.getId() == null) entityManager.persist(template);
        return template;
    }

    @Override
    public List<WhatsappTemplate> listAndSync(String connectionId) throws OperationFailedException {
        WhatsappConnection connection = connectionService.getLoggedInUserConnection(connectionId);
        String path = "/" + connection.getWabaId() + "/message_templates?fields=id,name,language,status,category,components&limit=100";
        while (path != null) {
            JsonNode response = metaGraphClient.get(path, token(connection));
            JsonNode data = response.path("data");
            if (data.isArray()) for (JsonNode item : data) upsert(connection, item);
            String next = text(response.path("paging"), "next");
            path = blank(next) ? null : graphPath(next);
        }
        return entityManager.createQuery("select t from WhatsappTemplate t where t.connection=:connection and t.recordStatus=:status order by t.dateCreated desc", WhatsappTemplate.class)
                .setParameter("connection", connection).setParameter("status", RecordStatus.ACTIVE).getResultList();
    }

    @Override
    public String uploadSample(String connectionId,String fileName,String contentType,byte[] content) throws ValidationFailedException,OperationFailedException {
        if(content==null||content.length==0)throw new ValidationFailedException("Choose a non-empty media file");
        if(content.length>16*1024*1024)throw new ValidationFailedException("Sample media must not exceed 16 MB");
        if(blank(contentType)||!(contentType.startsWith("image/")||contentType.startsWith("video/")||"application/pdf".equals(contentType)))throw new ValidationFailedException("Only images, MP4 videos and PDF documents are supported");
        WhatsappConnection connection=connectionService.getLoggedInUserConnection(connectionId);
        String encodedType;
        try{encodedType=URLEncoder.encode(contentType,"UTF-8");}catch(Exception e){throw new IllegalStateException(e);}
        JsonNode session=metaGraphClient.postEmpty("/"+configurationAppId()+"/uploads?file_length="+content.length+"&file_type="+encodedType,token(connection));
        String sessionId=text(session,"id");
        if(blank(sessionId))throw new IllegalStateException("Meta did not return an upload session ID");
        JsonNode uploaded=metaGraphClient.upload("/"+sessionId,token(connection),contentType,content);
        String handle=text(uploaded,"h");
        if(blank(handle))throw new IllegalStateException("Meta did not return a sample media handle");
        return handle;
    }

    @Autowired private MetaConfiguration metaConfiguration;
    private String configurationAppId(){return metaConfiguration.appId();}

    private void upsert(WhatsappConnection connection, JsonNode item) {
        String name = text(item, "name");
        WhatsappTemplate template = find(connection, name);
        if (template == null) template = new WhatsappTemplate();
        template.setConnection(connection);
        template.setName(name);
        template.setLanguage(blank(text(item,"language")) ? "en_US" : text(item,"language"));
        template.setMetaTemplateId(text(item, "id"));
        template.setApprovalStatus(status(text(item, "status")));
        try { template.setCategory(WhatsappEnums.TemplateCategory.valueOf(text(item, "category").toUpperCase(Locale.ENGLISH))); }
        catch (Exception ignored) { template.setCategory(WhatsappEnums.TemplateCategory.UTILITY); }
        String storedBody = body(item.path("components"));
        template.setBody(template.getCategory() == WhatsappEnums.TemplateCategory.AUTHENTICATION && blank(storedBody) ? authenticationBody() : storedBody);
        template.setComponentsJson(item.path("components").toString());
        template.setRecordStatus(RecordStatus.ACTIVE);
        if (template.getId() == null) entityManager.persist(template);
    }

    private List<Map<String,Object>> buildComponents(WhatsappTemplateRequest request) throws ValidationFailedException {
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        if (authentication(request)) {
            Map<String,Object> body = component("BODY");
            body.put("add_security_recommendation", request.isAddSecurityRecommendation());
            result.add(body);

            Map<String,Object> footer = component("FOOTER");
            footer.put("code_expiration_minutes", request.getCodeExpirationMinutes());
            result.add(footer);

            Map<String,Object> button = new LinkedHashMap<String,Object>();
            button.put("type", "OTP");
            button.put("otp_type", "COPY_CODE");
            button.put("text", request.getOtpButtonText().trim());
            Map<String,Object> buttons = component("BUTTONS");
            buttons.put("buttons", Collections.singletonList(button));
            result.add(buttons);
            return result;
        }
        String headerType = blank(request.getHeaderType()) ? "NONE" : request.getHeaderType().toUpperCase(Locale.ENGLISH);
        if (!"NONE".equals(headerType)) {
            Map<String,Object> header = component("HEADER");
            header.put("format", headerType);
            if ("TEXT".equals(headerType)) {
                require(request.getHeaderText(), "Header text is required");
                header.put("text", request.getHeaderText().trim());
                if (countVariables(request.getHeaderText()) > 0) header.put("example", singleton("header_text", Collections.singletonList(request.getHeaderSample())));
            } else {
                if (!Arrays.asList("IMAGE", "VIDEO", "DOCUMENT").contains(headerType)) throw new ValidationFailedException("Unsupported header type");
                require(request.getHeaderMediaHandle(), "A Meta media sample handle is required for the header");
                header.put("example", singleton("header_handle", Collections.singletonList(request.getHeaderMediaHandle())));
            }
            result.add(header);
        }
        Map<String,Object> body = component("BODY");
        body.put("text", request.getBody().trim());
        if (countVariables(request.getBody()) > 0) body.put("example", singleton("body_text", Collections.singletonList(request.getBodySampleValues())));
        result.add(body);
        if (!blank(request.getFooter())) { Map<String,Object> footer = component("FOOTER"); footer.put("text", request.getFooter().trim()); result.add(footer); }
        if (!request.getButtons().isEmpty()) {
            List<Map<String,Object>> buttons = new ArrayList<Map<String,Object>>();
            for (WhatsappTemplateRequest.Button value : request.getButtons()) {
                if (value == null || blank(value.getText())) continue;
                String type = blank(value.getType()) ? "QUICK_REPLY" : value.getType().toUpperCase(Locale.ENGLISH);
                Map<String,Object> button = new LinkedHashMap<String,Object>();
                button.put("type", type); button.put("text", value.getText().trim());
                if ("URL".equals(type)) { require(value.getValue(), "Website URL is required"); button.put("url", value.getValue().trim()); if (value.getValue().contains("{{1}}")) button.put("example", Collections.singletonList(value.getUrlSample())); }
                else if ("PHONE_NUMBER".equals(type)) { require(value.getValue(), "Phone number is required"); button.put("phone_number", value.getValue().trim()); }
                else if (!"QUICK_REPLY".equals(type)) throw new ValidationFailedException("Unsupported button type");
                buttons.add(button);
            }
            if (!buttons.isEmpty()) { Map<String,Object> group = component("BUTTONS"); group.put("buttons", buttons); result.add(group); }
        }
        return result;
    }

    private void validate(WhatsappTemplateRequest r) throws ValidationFailedException {
        if (r == null) throw new ValidationFailedException("Template details are required");
        require(r.getConnectionId(), "Select a WhatsApp account"); require(r.getName(), "Template name is required");
        if (r.getCategory() == null) throw new ValidationFailedException("Template category is required");
        if (authentication(r)) {
            if (r.getCodeExpirationMinutes() == null || r.getCodeExpirationMinutes() < 1 || r.getCodeExpirationMinutes() > 90) throw new ValidationFailedException("Code expiry must be between 1 and 90 minutes");
            require(r.getOtpButtonText(), "Copy-code button text is required");
            return;
        }
        require(r.getBody(), "Template body is required");
        int expected = countVariables(r.getBody());
        if (expected != r.getBodySampleValues().size()) throw new ValidationFailedException("Provide one sample value for each body variable (expected " + expected + ")");
        for (int i=1;i<=expected;i++) if (!r.getBody().contains("{{"+i+"}}")) throw new ValidationFailedException("Template variables must be sequential, starting at {{1}}");
    }
    private WhatsappTemplate find(WhatsappConnection c,String name){List<WhatsappTemplate> rows=entityManager.createQuery("select t from WhatsappTemplate t where t.connection=:c and t.name=:n",WhatsappTemplate.class).setParameter("c",c).setParameter("n",name).getResultList();return rows.isEmpty()?null:rows.get(0);}
    private String token(WhatsappConnection c){return tokenEncryptionService.decrypt(c.getAccessTokenCiphertext());}
    private Map<String,Object> component(String type){Map<String,Object> value=new LinkedHashMap<String,Object>();value.put("type",type);return value;}
    private Map<String,Object> singleton(String key,Object value){Map<String,Object> map=new LinkedHashMap<String,Object>();map.put(key,value);return map;}
    private int countVariables(String body){Set<Integer> indexes=new HashSet<Integer>();Matcher m=VARIABLE.matcher(body==null?"":body);while(m.find())indexes.add(Integer.valueOf(m.group(1)));return indexes.size();}
    private String body(JsonNode components){if(components.isArray())for(JsonNode c:components)if("BODY".equalsIgnoreCase(text(c,"type")))return text(c,"text");return "";}
    private WhatsappEnums.ApprovalStatus status(String value){try{return WhatsappEnums.ApprovalStatus.valueOf(value.toUpperCase(Locale.ENGLISH));}catch(Exception e){return WhatsappEnums.ApprovalStatus.UNKNOWN;}}
    private String text(JsonNode node,String field){JsonNode v=node==null?null:node.get(field);return v==null||v.isNull()?null:v.asText();}
    private String graphPath(String next){int marker=next.indexOf("/v");if(marker>=0){int slash=next.indexOf('/',marker+2);if(slash>=0)return next.substring(slash);}return next;}
    private String json(Object value){try{return mapper.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException("Could not store template components",e);}}
    private void require(String value,String message)throws ValidationFailedException{if(blank(value))throw new ValidationFailedException(message);}
    private boolean authentication(WhatsappTemplateRequest request){return request != null && request.getCategory() == WhatsappEnums.TemplateCategory.AUTHENTICATION;}
    private String authenticationBody(){return "{{1}} is your verification code.";}
    private boolean blank(String value){return value==null||value.trim().isEmpty();}
}
