package org.pahappa.systems.egoSms.models.billing;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import org.sers.webutils.model.BaseEntity;

@Entity
@Table(
    name = "whatsapp_billing_invoices",
    uniqueConstraints = @UniqueConstraint(columnNames = {"billing_account_id", "period_key"}))
public class WhatsappBillingInvoice extends BaseEntity {
  private static final long serialVersionUID = 1L;
  private WhatsappBillingAccount billingAccount;
  private String periodKey;
  private long messageCount;
  private BigDecimal estimatedMetaCost, markupAmount, monthlyFee, totalAmount;
  private BillingEnums.InvoiceStatus invoiceStatus = BillingEnums.InvoiceStatus.OPEN;
  private Date generatedAt, paidAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "billing_account_id", nullable = false)
  public WhatsappBillingAccount getBillingAccount() {
    return billingAccount;
  }

  public void setBillingAccount(WhatsappBillingAccount v) {
    billingAccount = v;
  }

  @Column(name = "period_key", nullable = false, length = 7)
  public String getPeriodKey() {
    return periodKey;
  }

  public void setPeriodKey(String v) {
    periodKey = v;
  }

  @Column(name = "message_count", nullable = false)
  public long getMessageCount() {
    return messageCount;
  }

  public void setMessageCount(long v) {
    messageCount = v;
  }

  @Column(name = "estimated_meta_cost", nullable = false, precision = 19, scale = 4)
  public BigDecimal getEstimatedMetaCost() {
    return estimatedMetaCost;
  }

  public void setEstimatedMetaCost(BigDecimal v) {
    estimatedMetaCost = v;
  }

  @Column(name = "markup_amount", nullable = false, precision = 19, scale = 4)
  public BigDecimal getMarkupAmount() {
    return markupAmount;
  }

  public void setMarkupAmount(BigDecimal v) {
    markupAmount = v;
  }

  @Column(name = "monthly_fee", nullable = false, precision = 19, scale = 4)
  public BigDecimal getMonthlyFee() {
    return monthlyFee;
  }

  public void setMonthlyFee(BigDecimal v) {
    monthlyFee = v;
  }

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal v) {
    totalAmount = v;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "invoice_status", nullable = false, length = 20)
  public BillingEnums.InvoiceStatus getInvoiceStatus() {
    return invoiceStatus;
  }

  public void setInvoiceStatus(BillingEnums.InvoiceStatus v) {
    invoiceStatus = v;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "generated_at", nullable = false)
  public Date getGeneratedAt() {
    return generatedAt;
  }

  public void setGeneratedAt(Date v) {
    generatedAt = v;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "paid_at")
  public Date getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(Date v) {
    paidAt = v;
  }
}
