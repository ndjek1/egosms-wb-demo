package org.pahappa.systems.egoSms.models.whatsapp;

import org.sers.webutils.model.BaseEntity;
import javax.persistence.*;

@Entity @Table(name="whatsapp_templates", uniqueConstraints={@UniqueConstraint(columnNames={"connection_id","template_name"})})
public class WhatsappTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private WhatsappConnection connection;
    private String name, language="en_US", metaTemplateId, body, componentsJson;
    private WhatsappEnums.TemplateCategory category;
    private WhatsappEnums.ApprovalStatus approvalStatus=WhatsappEnums.ApprovalStatus.PENDING;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="connection_id") public WhatsappConnection getConnection(){return connection;} public void setConnection(WhatsappConnection v){connection=v;}
    @Column(name="template_name",nullable=false,length=512) public String getName(){return name;} public void setName(String v){name=v;}
    @Column(name="language_code",nullable=false,length=16) public String getLanguage(){return language;} public void setLanguage(String v){language=v;}
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) public WhatsappEnums.TemplateCategory getCategory(){return category;} public void setCategory(WhatsappEnums.TemplateCategory v){category=v;}
    @Column(name="meta_template_id",length=128) public String getMetaTemplateId(){return metaTemplateId;} public void setMetaTemplateId(String v){metaTemplateId=v;}
    @Enumerated(EnumType.STRING) @Column(name="approval_status",nullable=false,length=32) public WhatsappEnums.ApprovalStatus getApprovalStatus(){return approvalStatus;} public void setApprovalStatus(WhatsappEnums.ApprovalStatus v){approvalStatus=v;}
    @Lob @Column(nullable=false) public String getBody(){return body;} public void setBody(String v){body=v;}
    @Lob @Column(name="components_json") public String getComponentsJson(){return componentsJson;} public void setComponentsJson(String v){componentsJson=v;}
}
