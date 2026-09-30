package org.pahappa.systems.egoSms.views.whatsapp;

import org.pahappa.systems.egoSms.core.services.whatsapp.*;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import java.util.Base64;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ManagedBean(name="whatsappWorkspace")
@SessionScoped
public class WhatsappWorkspaceView implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{\\{(\\d+)\\}\\}");
    private transient WhatsappConnectionService connectionService;
    private transient WhatsappTemplateService templateService;
    private transient WhatsappSendService sendService;
    private transient WhatsappDataService dataService;
    private List<WhatsappConnection> connections = new ArrayList<WhatsappConnection>();
    private List<WhatsappConnection> accountConnections = new ArrayList<WhatsappConnection>();
    private List<WhatsappTemplate> templates = new ArrayList<WhatsappTemplate>();
    private List<WhatsappConversation> conversations = new ArrayList<WhatsappConversation>();
    private List<WhatsappMessage> messages = new ArrayList<WhatsappMessage>();
    private String connectionId;
    private WhatsappTemplateRequest templateRequest = new WhatsappTemplateRequest();
    private WhatsappSendRequest sendRequest = new WhatsappSendRequest();
    private String bodySamples;
    private String sendVariables;
    private String buttonVariables;
    private WhatsappConversation selectedConversation;
    private String replyBody;
    private String mediaPreviewDataUrl;
    private String mediaFileName;
    private List<SendVariable> sendBodyInputs = new ArrayList<SendVariable>();
    private List<SendVariable> sendButtonInputs = new ArrayList<SendVariable>();
    private SendVariable sendHeaderInput;
    private String sendMediaFileName;
    private String sendMediaPreviewDataUrl;
    private String sendRecipients;
    private List<BulkSendResult> bulkSendResults = new ArrayList<BulkSendResult>();
    private String latestInboundMessageId;
    private String latestInboundSender;
    private String latestInboundBody;
    private Map<String,Long> unreadCounts = new LinkedHashMap<String,Long>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class SendVariable implements Serializable {
        private static final long serialVersionUID = 1L;
        private int position;
        private String label;
        private String example;
        private String value;
        public SendVariable(){}
        public SendVariable(int position,String label,String example){this.position=position;this.label=label;this.example=example;}
        public int getPosition(){return position;} public void setPosition(int v){position=v;}
        public String getLabel(){return label;} public void setLabel(String v){label=v;}
        public String getExample(){return example;} public void setExample(String v){example=v;}
        public String getValue(){return value;} public void setValue(String v){value=v;}
    }

    public static class BulkSendResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String recipient;
        private final boolean successful;
        private final String details;

        public BulkSendResult(String recipient, boolean successful, String details) {
            this.recipient = recipient;
            this.successful = successful;
            this.details = details;
        }

        public String getRecipient(){return recipient;}
        public boolean isSuccessful(){return successful;}
        public String getDetails(){return details;}
    }

    @PostConstruct public void init(){wire();reloadConnections();}
    public void reloadConnections(){wire();connections=connectionService.getLoggedInUserConnections();accountConnections=connectionService.getLoggedInUserAllConnections();boolean selectedStillConnected=false;for(WhatsappConnection connection:connections)if(connection.getId().equals(connectionId)){selectedStillConnected=true;break;}if(!selectedStillConnected)connectionId=connections.isEmpty()?null:connections.get(0).getId();refreshLocal();}
    public void disconnectConnection(WhatsappConnection connection){wire();try{connectionService.disconnect(connection.getId());reloadConnections();UiUtils.showMessageBox("WhatsApp disconnected","The number was disconnected from egoSms only. It remains registered in Meta.");}catch(Exception e){fail(e);}}
    private void wire(){if(connectionService==null){connectionService=ApplicationContextProvider.getBean(WhatsappConnectionService.class);templateService=ApplicationContextProvider.getBean(WhatsappTemplateService.class);sendService=ApplicationContextProvider.getBean(WhatsappSendService.class);dataService=ApplicationContextProvider.getBean(WhatsappDataService.class);}}
    public void changeConnection(){selectedConversation=null;messages.clear();sendRequest.setTemplateId(null);sendHeaderInput=null;removeSendHeaderMedia();sendBodyInputs.clear();sendButtonInputs.clear();refreshLocal();}
    public void refreshLocal(){wire();if(blank(connectionId)){templates.clear();conversations.clear();unreadCounts.clear();clearNotificationSnapshot();return;}try{templates=dataService.getTemplates(connectionId);conversations=dataService.getConversations(connectionId);unreadCounts=dataService.getUnreadCounts(connectionId);refreshNotificationSnapshot();}catch(Exception e){fail(e);}}
    public void syncTemplates(){wire();try{templates=templateService.listAndSync(connectionId);UiUtils.showMessageBox("Templates synchronized",templates.size()+" template(s) loaded from Meta");}catch(Exception e){fail(e);}}
    public void createTemplate(){wire();try{templateRequest.setConnectionId(connectionId);templateRequest.setBodySampleValues(isAuthenticationTemplateRequest()?Collections.<String>emptyList():lines(bodySamples));templateService.create(templateRequest);templateRequest=new WhatsappTemplateRequest();bodySamples=null;mediaPreviewDataUrl=null;mediaFileName=null;templates=templateService.listAndSync(connectionId);UiUtils.showMessageBox("Template submitted","Meta is reviewing the new template");}catch(Exception e){fail(e);}}
    public void uploadTemplateSample(FileUploadEvent event){wire();try{if(blank(connectionId))throw new IllegalStateException("Select a connected WhatsApp account first");UploadedFile file=event.getFile();String type=file.getContentType();validateMediaType(type);byte[] content=file.getContent();templateRequest.setHeaderMediaHandle(templateService.uploadSample(connectionId,file.getFileName(),type,content));mediaFileName=file.getFileName();mediaPreviewDataUrl=(type.startsWith("image/")||type.startsWith("video/"))?"data:"+type+";base64,"+Base64.getEncoder().encodeToString(content):null;UiUtils.showMessageBox("Media uploaded","The sample is ready for Meta review");}catch(Exception e){templateRequest.setHeaderMediaHandle(null);mediaFileName=null;mediaPreviewDataUrl=null;fail(e);}}
    public void removeTemplateSample(){templateRequest.setHeaderMediaHandle(null);mediaFileName=null;mediaPreviewDataUrl=null;}
    public void headerTypeChanged(){removeTemplateSample();}
    public void categoryChanged(){if(isAuthenticationTemplateRequest()){templateRequest.setHeaderType("NONE");templateRequest.setHeaderText(null);templateRequest.setHeaderSample(null);templateRequest.setFooter(null);templateRequest.getButtons().clear();bodySamples=null;removeTemplateSample();}}
    private void validateMediaType(String type){String expected=templateRequest.getHeaderType();if("IMAGE".equals(expected)&&(type==null||!type.startsWith("image/")))throw new IllegalArgumentException("Choose an image for an image header");if("VIDEO".equals(expected)&&!"video/mp4".equals(type))throw new IllegalArgumentException("Choose an MP4 file for a video header");if("DOCUMENT".equals(expected)&&!"application/pdf".equals(type))throw new IllegalArgumentException("Choose a PDF file for a document header");}
    public void addButton(){templateRequest.getButtons().add(new WhatsappTemplateRequest.Button());}
    public void removeButton(WhatsappTemplateRequest.Button button){templateRequest.getButtons().remove(button);}
    public void templateChanged(){
        sendHeaderInput=null;removeSendHeaderMedia();sendBodyInputs.clear();sendButtonInputs.clear();
        WhatsappTemplate template=getSelectedSendTemplate();if(template==null)return;
        JsonNode components=components(template);
        JsonNode header=component(components,"HEADER");
        if(header!=null&&"TEXT".equalsIgnoreCase(header.path("format").asText())&&header.path("text").asText("").contains("{{1}}")){JsonNode samples=header.path("example").path("header_text");String example=samples.isArray()&&samples.size()>0?samples.get(0).asText(null):null;sendHeaderInput=new SendVariable(1,"Value for header {{1}}",example);}
        JsonNode body=component(components,"BODY");
        if(template.getCategory()==WhatsappEnums.TemplateCategory.AUTHENTICATION){sendBodyInputs.add(new SendVariable(1,"One-time password","123456"));}
        else {List<String> examples=bodyExamples(body);SortedSet<Integer> positions=variablePositions(template.getBody());for(Integer position:positions)sendBodyInputs.add(new SendVariable(position,"Value for {{"+position+"}}",position<=examples.size()?examples.get(position-1):null));}
        JsonNode buttons=component(components,"BUTTONS");int dynamicNumber=1;
        if(buttons!=null&&buttons.path("buttons").isArray())for(JsonNode button:buttons.path("buttons"))if("URL".equalsIgnoreCase(button.path("type").asText())&&button.path("url").asText("").contains("{{1}}")){String text=button.path("text").asText("Website button");String example=button.path("example").isArray()&&button.path("example").size()>0?button.path("example").get(0).asText():null;sendButtonInputs.add(new SendVariable(dynamicNumber++,"URL value for “"+text+"”",example));}
    }
    public void uploadSendHeaderMedia(FileUploadEvent event){wire();try{UploadedFile file=event.getFile();String type=file.getContentType();validateSendMediaType(type);byte[] content=file.getContent();sendRequest.setHeaderMediaId(sendService.uploadHeaderMedia(connectionId,file.getFileName(),type,content));sendMediaFileName=file.getFileName();sendMediaPreviewDataUrl=(type.startsWith("image/")||type.startsWith("video/"))?"data:"+type+";base64,"+Base64.getEncoder().encodeToString(content):null;UiUtils.showMessageBox("Media uploaded","The header media is ready to send");}catch(Exception e){removeSendHeaderMedia();fail(e);}}
    public void removeSendHeaderMedia(){sendRequest.setHeaderMediaId(null);sendMediaFileName=null;sendMediaPreviewDataUrl=null;}
    private void validateSendMediaType(String type){String format=getSendPreviewHeaderFormat();if("IMAGE".equalsIgnoreCase(format)&&(type==null||!type.startsWith("image/")))throw new IllegalArgumentException("Choose an image for this template");if("VIDEO".equalsIgnoreCase(format)&&!"video/mp4".equals(type))throw new IllegalArgumentException("Choose an MP4 video for this template");if("DOCUMENT".equalsIgnoreCase(format)&&!"application/pdf".equals(type))throw new IllegalArgumentException("Choose a PDF document for this template");}
    public void send(){
        wire();
        bulkSendResults.clear();
        try {
            List<String> recipients=parseRecipients(sendRecipients);
            if(recipients.isEmpty())throw new IllegalArgumentException("Enter at least one recipient number");
            if(recipients.size()>100)throw new IllegalArgumentException("A batch can contain at most 100 recipient numbers");
            if(isSelectedSendTemplateAuthentication()&&recipients.size()>1)throw new IllegalArgumentException("Authentication messages must be sent to one recipient at a time so each customer receives their own OTP");

            sendRequest.setConnectionId(connectionId);
            sendRequest.setHeaderVariable(sendHeaderInput==null?null:sendHeaderInput.getValue());
            sendRequest.setBodyVariables(inputValues(sendBodyInputs));
            sendRequest.setButtonVariables(inputValues(sendButtonInputs));

            List<String> failedRecipients=new ArrayList<String>();
            for(String recipient:recipients){
                try {
                    sendRequest.setRecipientNumber(recipient);
                    sendService.send(sendRequest);
                    bulkSendResults.add(new BulkSendResult(recipient,true,"Accepted by Meta"));
                } catch(Exception recipientError) {
                    failedRecipients.add(recipient);
                    bulkSendResults.add(new BulkSendResult(recipient,false,root(recipientError)));
                }
            }

            int sent=recipients.size()-failedRecipients.size();
            conversations=dataService.getConversations(connectionId);
            if(failedRecipients.isEmpty()){
                resetSendComposer();
                UiUtils.showMessageBox("Batch sent",sent+" message(s) accepted by Meta");
            } else {
                sendRecipients=joinLines(failedRecipients);
                UiUtils.showMessageBox("Batch completed",sent+" sent, "+failedRecipients.size()+" failed. Failed numbers remain in the recipient field for retry.");
            }
        } catch(Exception e){
            fail(e);
        }
    }

    private void resetSendComposer(){
        sendRequest=new WhatsappSendRequest();
        sendRecipients=null;
        sendVariables=null;
        buttonVariables=null;
        sendHeaderInput=null;
        sendMediaFileName=null;
        sendMediaPreviewDataUrl=null;
        sendBodyInputs.clear();
        sendButtonInputs.clear();
    }

    private List<String> parseRecipients(String value){
        LinkedHashSet<String> unique=new LinkedHashSet<String>();
        if(value!=null)for(String entry:value.split("[,;\\r\\n]+")){
            String normalized=entry.trim().replaceAll("[^0-9]","");
            if(!normalized.isEmpty())unique.add(normalized);
        }
        return new ArrayList<String>(unique);
    }

    private String joinLines(List<String> values){StringBuilder result=new StringBuilder();for(String value:values){if(result.length()>0)result.append('\n');result.append(value);}return result.toString();}
    public void openConversation(WhatsappConversation conversation){wire();selectedConversation=conversation;try{messages=dataService.getMessages(conversation.getId());dataService.markConversationRead(conversation.getId());unreadCounts=dataService.getUnreadCounts(connectionId);}catch(Exception e){fail(e);}}
    public void refreshConversation(){if(selectedConversation!=null)openConversation(selectedConversation);}
    public void pollConversations(){wire();if(blank(connectionId))return;try{conversations=dataService.getConversations(connectionId);if(selectedConversation!=null)messages=dataService.getMessages(selectedConversation.getId());unreadCounts=dataService.getUnreadCounts(connectionId);refreshNotificationSnapshot();}catch(Exception e){fail(e);}}
    private void refreshNotificationSnapshot()throws Exception{WhatsappMessage latest=dataService.getLatestInboundMessage(connectionId);if(latest==null){clearNotificationSnapshot();return;}latestInboundMessageId=blank(latest.getMetaMessageId())?latest.getId():latest.getMetaMessageId();WhatsappConversation conversation=latest.getConversation();latestInboundSender=blank(conversation.getContactName())?conversation.getContactNumber():conversation.getContactName();latestInboundBody=latest.getBody();}
    private void clearNotificationSnapshot(){latestInboundMessageId=null;latestInboundSender=null;latestInboundBody=null;}
    public void reply(){wire();if(selectedConversation==null)return;try{WhatsappSendRequest request=new WhatsappSendRequest();request.setConnectionId(connectionId);request.setRecipientNumber(selectedConversation.getContactNumber());request.setRawBody(replyBody);sendService.send(request);replyBody=null;openConversation(selectedConversation);}catch(Exception e){fail(e);}}
    public String previewBody(){if(isAuthenticationTemplateRequest())return "123456 is your verification code."+(templateRequest.isAddSecurityRecommendation()?"\nFor your security, do not share this code.":"");String body=templateRequest.getBody();if(body==null||body.trim().isEmpty())return "Your message preview";List<String> samples=lines(bodySamples);for(int i=0;i<samples.size();i++)body=body.replace("{{"+(i+1)+"}}",samples.get(i));return body;}
    public String previewHeader(){String header=templateRequest.getHeaderText();if(header==null)return "";String sample=templateRequest.getHeaderSample();return sample==null?header:header.replace("{{1}}",sample);}
    public WhatsappTemplate getSelectedSendTemplate(){if(blank(sendRequest.getTemplateId()))return null;for(WhatsappTemplate value:templates)if(sendRequest.getTemplateId().equals(value.getId()))return value;return null;}
    public boolean isAuthenticationTemplateRequest(){return templateRequest != null && templateRequest.getCategory()==WhatsappEnums.TemplateCategory.AUTHENTICATION;}
    public boolean isSelectedSendTemplateAuthentication(){WhatsappTemplate template=getSelectedSendTemplate();return template!=null&&template.getCategory()==WhatsappEnums.TemplateCategory.AUTHENTICATION;}
    public String getSendPreviewBody(){WhatsappTemplate template=getSelectedSendTemplate();if(template==null)return "Choose an approved template to preview it";String body=template.getBody()==null?"":template.getBody();for(SendVariable input:sendBodyInputs)if(!blank(input.getValue()))body=body.replace("{{"+input.getPosition()+"}}",input.getValue());return body;}
    public String getSendPreviewHeader(){JsonNode header=component(components(getSelectedSendTemplate()),"HEADER");if(header==null)return null;String text=header.path("text").asText(null);if(text!=null&&sendHeaderInput!=null&&!blank(sendHeaderInput.getValue()))text=text.replace("{{1}}",sendHeaderInput.getValue());return text;}
    public String getSendPreviewHeaderFormat(){JsonNode header=component(components(getSelectedSendTemplate()),"HEADER");return header==null?null:header.path("format").asText(null);}
    public String getSendPreviewFooter(){JsonNode footer=component(components(getSelectedSendTemplate()),"FOOTER");if(footer==null)return null;if(footer.has("code_expiration_minutes"))return "This code expires in "+footer.path("code_expiration_minutes").asInt(10)+" minutes.";return footer.path("text").asText(null);}
    public List<String> getSendPreviewButtons(){List<String> result=new ArrayList<String>();JsonNode buttons=component(components(getSelectedSendTemplate()),"BUTTONS");if(buttons!=null&&buttons.path("buttons").isArray())for(JsonNode button:buttons.path("buttons"))result.add(button.path("text").asText("Button"));return result;}
    public int templateCount(String status){if(blank(status)||"ALL".equalsIgnoreCase(status))return templates.size();int count=0;for(WhatsappTemplate value:templates)if(value.getApprovalStatus()!=null&&status.equalsIgnoreCase(value.getApprovalStatus().name()))count++;return count;}
    public String templateHeaderFormat(WhatsappTemplate template){JsonNode header=component(components(template),"HEADER");return header==null?null:header.path("format").asText(null);}
    public String templateHeaderText(WhatsappTemplate template){JsonNode header=component(components(template),"HEADER");return header==null?null:header.path("text").asText(null);}
    public String templateFooter(WhatsappTemplate template){JsonNode footer=component(components(template),"FOOTER");return footer==null?null:footer.path("text").asText(null);}
    public List<String> templateButtons(WhatsappTemplate template){List<String> result=new ArrayList<String>();JsonNode buttons=component(components(template),"BUTTONS");if(buttons!=null&&buttons.path("buttons").isArray())for(JsonNode button:buttons.path("buttons"))result.add(button.path("text").asText("Button"));return result;}
    public String formatConversationTime(Date value){return value==null?"":new SimpleDateFormat("dd MMM HH:mm").format(value);}
    public long unreadCount(WhatsappConversation conversation){if(conversation==null)return 0;Long value=unreadCounts.get(conversation.getId());return value==null?0:value;}
    public long getTotalUnreadCount(){long total=0;for(Long value:unreadCounts.values())if(value!=null)total+=value;return total;}
    public boolean isConnected(){return !connections.isEmpty();}
    private List<String> lines(String value){List<String> result=new ArrayList<String>();if(value!=null)for(String line:value.split("\\r?\\n"))if(!line.trim().isEmpty())result.add(line.trim());return result;}
    private List<String> inputValues(List<SendVariable> inputs){List<String> result=new ArrayList<String>();for(SendVariable input:inputs)result.add(input.getValue()==null?"":input.getValue().trim());return result;}
    private SortedSet<Integer> variablePositions(String value){SortedSet<Integer> result=new TreeSet<Integer>();Matcher matcher=TEMPLATE_VARIABLE.matcher(value==null?"":value);while(matcher.find())result.add(Integer.valueOf(matcher.group(1)));return result;}
    private JsonNode components(WhatsappTemplate template){if(template==null||blank(template.getComponentsJson()))return objectMapper.createArrayNode();try{return objectMapper.readTree(template.getComponentsJson());}catch(Exception ignored){return objectMapper.createArrayNode();}}
    private JsonNode component(JsonNode components,String type){if(components!=null&&components.isArray())for(JsonNode value:components)if(type.equalsIgnoreCase(value.path("type").asText()))return value;return null;}
    private List<String> bodyExamples(JsonNode body){List<String> result=new ArrayList<String>();if(body==null)return result;JsonNode rows=body.path("example").path("body_text");if(rows.isArray()&&rows.size()>0&&rows.get(0).isArray())for(JsonNode value:rows.get(0))result.add(value.asText());return result;}
    private void fail(Exception e){UiUtils.ComposeFailure("WhatsApp action failed",root(e));}
    private String root(Throwable e){Throwable value=e;while(value.getCause()!=null)value=value.getCause();return value.getMessage()==null?value.toString():value.getMessage();}
    private boolean blank(String value){return value==null||value.trim().isEmpty();}

    public List<WhatsappConnection> getConnections(){return connections;} public void setConnections(List<WhatsappConnection> v){connections=v;}
    public List<WhatsappConnection> getAccountConnections(){return accountConnections;} public void setAccountConnections(List<WhatsappConnection> v){accountConnections=v;}
    public List<WhatsappTemplate> getTemplates(){return templates;} public void setTemplates(List<WhatsappTemplate> v){templates=v;}
    public List<WhatsappTemplate> getApprovedTemplates(){List<WhatsappTemplate> result=new ArrayList<WhatsappTemplate>();for(WhatsappTemplate value:templates)if(value.getApprovalStatus()==WhatsappEnums.ApprovalStatus.APPROVED)result.add(value);return result;}
    public List<WhatsappConversation> getConversations(){return conversations;} public void setConversations(List<WhatsappConversation> v){conversations=v;}
    public List<WhatsappMessage> getMessages(){return messages;} public void setMessages(List<WhatsappMessage> v){messages=v;}
    public String getConnectionId(){return connectionId;} public void setConnectionId(String v){connectionId=v;}
    public WhatsappTemplateRequest getTemplateRequest(){return templateRequest;} public void setTemplateRequest(WhatsappTemplateRequest v){templateRequest=v;}
    public WhatsappSendRequest getSendRequest(){return sendRequest;} public void setSendRequest(WhatsappSendRequest v){sendRequest=v;}
    public String getBodySamples(){return bodySamples;} public void setBodySamples(String v){bodySamples=v;}
    public String getSendVariables(){return sendVariables;} public void setSendVariables(String v){sendVariables=v;}
    public String getButtonVariables(){return buttonVariables;} public void setButtonVariables(String v){buttonVariables=v;}
    public WhatsappConversation getSelectedConversation(){return selectedConversation;} public void setSelectedConversation(WhatsappConversation v){selectedConversation=v;}
    public String getReplyBody(){return replyBody;} public void setReplyBody(String v){replyBody=v;}
    public String getMediaPreviewDataUrl(){return mediaPreviewDataUrl;} public void setMediaPreviewDataUrl(String v){mediaPreviewDataUrl=v;}
    public String getMediaFileName(){return mediaFileName;} public void setMediaFileName(String v){mediaFileName=v;}
    public List<SendVariable> getSendBodyInputs(){return sendBodyInputs;} public void setSendBodyInputs(List<SendVariable> v){sendBodyInputs=v;}
    public List<SendVariable> getSendButtonInputs(){return sendButtonInputs;} public void setSendButtonInputs(List<SendVariable> v){sendButtonInputs=v;}
    public SendVariable getSendHeaderInput(){return sendHeaderInput;} public void setSendHeaderInput(SendVariable v){sendHeaderInput=v;}
    public String getSendMediaFileName(){return sendMediaFileName;} public void setSendMediaFileName(String v){sendMediaFileName=v;}
    public String getSendMediaPreviewDataUrl(){return sendMediaPreviewDataUrl;} public void setSendMediaPreviewDataUrl(String v){sendMediaPreviewDataUrl=v;}
    public String getSendRecipients(){return sendRecipients;} public void setSendRecipients(String v){sendRecipients=v;}
    public List<BulkSendResult> getBulkSendResults(){return bulkSendResults;} public void setBulkSendResults(List<BulkSendResult> v){bulkSendResults=v;}
    public String getLatestInboundMessageId(){return latestInboundMessageId;}
    public String getLatestInboundSender(){return latestInboundSender;}
    public String getLatestInboundBody(){return latestInboundBody;}
}
