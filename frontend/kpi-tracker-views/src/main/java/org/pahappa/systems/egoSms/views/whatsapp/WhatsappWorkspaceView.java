package org.pahappa.systems.egoSms.views.whatsapp;

import org.pahappa.systems.egoSms.core.services.whatsapp.*;
import org.pahappa.systems.egoSms.models.whatsapp.*;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name="whatsappWorkspace")
@SessionScoped
public class WhatsappWorkspaceView implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient WhatsappConnectionService connectionService;
    private transient WhatsappTemplateService templateService;
    private transient WhatsappSendService sendService;
    private transient WhatsappDataService dataService;
    private List<WhatsappConnection> connections = new ArrayList<WhatsappConnection>();
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

    @PostConstruct public void init(){wire();connections=connectionService.getLoggedInUserConnections();if(!connections.isEmpty()){connectionId=connections.get(0).getId();refreshLocal();}}
    public void reloadConnections(){wire();connections=connectionService.getLoggedInUserConnections();if(blank(connectionId)&&!connections.isEmpty())connectionId=connections.get(0).getId();refreshLocal();}
    private void wire(){if(connectionService==null){connectionService=ApplicationContextProvider.getBean(WhatsappConnectionService.class);templateService=ApplicationContextProvider.getBean(WhatsappTemplateService.class);sendService=ApplicationContextProvider.getBean(WhatsappSendService.class);dataService=ApplicationContextProvider.getBean(WhatsappDataService.class);}}
    public void changeConnection(){selectedConversation=null;messages.clear();refreshLocal();}
    public void refreshLocal(){wire();if(blank(connectionId)){templates.clear();conversations.clear();return;}try{templates=dataService.getTemplates(connectionId);conversations=dataService.getConversations(connectionId);}catch(Exception e){fail(e);}}
    public void syncTemplates(){wire();try{templates=templateService.listAndSync(connectionId);UiUtils.showMessageBox("Templates synchronized",templates.size()+" template(s) loaded from Meta");}catch(Exception e){fail(e);}}
    public void createTemplate(){wire();try{templateRequest.setConnectionId(connectionId);templateRequest.setBodySampleValues(lines(bodySamples));templateService.create(templateRequest);templateRequest=new WhatsappTemplateRequest();bodySamples=null;templates=templateService.listAndSync(connectionId);UiUtils.showMessageBox("Template submitted","Meta is reviewing the new template");}catch(Exception e){fail(e);}}
    public void addButton(){templateRequest.getButtons().add(new WhatsappTemplateRequest.Button());}
    public void removeButton(WhatsappTemplateRequest.Button button){templateRequest.getButtons().remove(button);}
    public void send(){wire();try{sendRequest.setConnectionId(connectionId);sendRequest.setBodyVariables(lines(sendVariables));sendRequest.setButtonVariables(lines(buttonVariables));sendService.send(sendRequest);sendRequest=new WhatsappSendRequest();sendVariables=null;buttonVariables=null;conversations=dataService.getConversations(connectionId);UiUtils.showMessageBox("Message sent","Meta accepted the message");}catch(Exception e){fail(e);}}
    public void openConversation(WhatsappConversation conversation){wire();selectedConversation=conversation;try{messages=dataService.getMessages(conversation.getId());}catch(Exception e){fail(e);}}
    public void refreshConversation(){if(selectedConversation!=null)openConversation(selectedConversation);}
    public void reply(){wire();if(selectedConversation==null)return;try{WhatsappSendRequest request=new WhatsappSendRequest();request.setConnectionId(connectionId);request.setRecipientNumber(selectedConversation.getContactNumber());request.setRawBody(replyBody);sendService.send(request);replyBody=null;openConversation(selectedConversation);}catch(Exception e){fail(e);}}
    public String previewBody(){String body=templateRequest.getBody();if(body==null||body.trim().isEmpty())return "Your message preview";List<String> samples=lines(bodySamples);for(int i=0;i<samples.size();i++)body=body.replace("{{"+(i+1)+"}}",samples.get(i));return body;}
    public String previewHeader(){String header=templateRequest.getHeaderText();if(header==null)return "";String sample=templateRequest.getHeaderSample();return sample==null?header:header.replace("{{1}}",sample);}
    public boolean isConnected(){return !connections.isEmpty();}
    private List<String> lines(String value){List<String> result=new ArrayList<String>();if(value!=null)for(String line:value.split("\\r?\\n"))if(!line.trim().isEmpty())result.add(line.trim());return result;}
    private void fail(Exception e){UiUtils.ComposeFailure("WhatsApp action failed",root(e));}
    private String root(Throwable e){Throwable value=e;while(value.getCause()!=null)value=value.getCause();return value.getMessage()==null?value.toString():value.getMessage();}
    private boolean blank(String value){return value==null||value.trim().isEmpty();}

    public List<WhatsappConnection> getConnections(){return connections;} public void setConnections(List<WhatsappConnection> v){connections=v;}
    public List<WhatsappTemplate> getTemplates(){return templates;} public void setTemplates(List<WhatsappTemplate> v){templates=v;}
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
}
