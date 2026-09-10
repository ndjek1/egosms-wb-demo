package org.pahappa.systems.egoSms.models.whatsapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WhatsappSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String connectionId;
    private String recipientNumber;
    private String templateId;
    private String rawBody;
    private List<String> bodyVariables = new ArrayList<String>();
    private String headerMediaId;
    private List<String> buttonVariables = new ArrayList<String>();

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getRecipientNumber() { return recipientNumber; }
    public void setRecipientNumber(String recipientNumber) { this.recipientNumber = recipientNumber; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getRawBody() { return rawBody; }
    public void setRawBody(String rawBody) { this.rawBody = rawBody; }
    public List<String> getBodyVariables() { return bodyVariables; }
    public void setBodyVariables(List<String> values) { bodyVariables = values == null ? new ArrayList<String>() : values; }
    public String getHeaderMediaId() { return headerMediaId; }
    public void setHeaderMediaId(String headerMediaId) { this.headerMediaId = headerMediaId; }
    public List<String> getButtonVariables() { return buttonVariables; }
    public void setButtonVariables(List<String> values) { buttonVariables = values == null ? new ArrayList<String>() : values; }
}
