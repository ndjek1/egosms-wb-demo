package org.pahappa.systems.egoSms.models.whatsapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WhatsappTemplateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String connectionId;
    private String name;
    private WhatsappEnums.TemplateCategory category = WhatsappEnums.TemplateCategory.UTILITY;
    private String language = "en_US";
    private String body;
    private List<String> bodySampleValues = new ArrayList<String>();
    private String headerType = "NONE";
    private String headerText;
    private String headerSample;
    private String headerMediaHandle;
    private String footer;
    private List<Button> buttons = new ArrayList<Button>();

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public WhatsappEnums.TemplateCategory getCategory() { return category; }
    public void setCategory(WhatsappEnums.TemplateCategory category) { this.category = category; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public List<String> getBodySampleValues() { return bodySampleValues; }
    public void setBodySampleValues(List<String> values) { bodySampleValues = values == null ? new ArrayList<String>() : values; }
    public String getHeaderType() { return headerType; }
    public void setHeaderType(String headerType) { this.headerType = headerType; }
    public String getHeaderText() { return headerText; }
    public void setHeaderText(String headerText) { this.headerText = headerText; }
    public String getHeaderSample() { return headerSample; }
    public void setHeaderSample(String headerSample) { this.headerSample = headerSample; }
    public String getHeaderMediaHandle() { return headerMediaHandle; }
    public void setHeaderMediaHandle(String headerMediaHandle) { this.headerMediaHandle = headerMediaHandle; }
    public String getFooter() { return footer; }
    public void setFooter(String footer) { this.footer = footer; }
    public List<Button> getButtons() { return buttons; }
    public void setButtons(List<Button> buttons) { this.buttons = buttons == null ? new ArrayList<Button>() : buttons; }

    public static class Button implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type = "QUICK_REPLY";
        private String text;
        private String value;
        private String urlSample;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getUrlSample() { return urlSample; }
        public void setUrlSample(String urlSample) { this.urlSample = urlSample; }
    }
}
