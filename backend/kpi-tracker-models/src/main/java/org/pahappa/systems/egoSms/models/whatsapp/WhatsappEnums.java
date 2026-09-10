package org.pahappa.systems.egoSms.models.whatsapp;

public final class WhatsappEnums {
    private WhatsappEnums() { }
    public enum TemplateCategory { UTILITY, MARKETING, AUTHENTICATION }
    public enum ApprovalStatus { PENDING, APPROVED, REJECTED, PAUSED, DISABLED, UNKNOWN }
    public enum Direction { OUTBOUND, INBOUND }
    public enum MessageStatus { QUEUED, SENT, DELIVERED, FAILED, READ }
}
