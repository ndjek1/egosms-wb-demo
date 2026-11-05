package org.pahappa.systems.egoSms.models.billing;

import java.math.BigDecimal;
import javax.persistence.*;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.sers.webutils.model.BaseEntity;

@Entity
@Table(
    name = "whatsapp_billing_accounts",
    uniqueConstraints = @UniqueConstraint(columnNames = "connection_id"))
public class WhatsappBillingAccount extends BaseEntity {
  private static final long serialVersionUID = 1L;
  private WhatsappConnection connection;
  private String currency = "USD";
  private BigDecimal monthlyFee = new BigDecimal("20.00"),
      estimatedMetaCostPerMessage = new BigDecimal("0.0300"),
      markupPercent = new BigDecimal("10.00");
  private BillingEnums.AccountStatus accountStatus = BillingEnums.AccountStatus.ACTIVE;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "connection_id", nullable = false)
  public WhatsappConnection getConnection() {
    return connection;
  }

  public void setConnection(WhatsappConnection v) {
    connection = v;
  }

  @Column(nullable = false, length = 3)
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String v) {
    currency = v;
  }

  @Column(name = "monthly_fee", nullable = false, precision = 19, scale = 4)
  public BigDecimal getMonthlyFee() {
    return monthlyFee;
  }

  public void setMonthlyFee(BigDecimal v) {
    monthlyFee = v;
  }

  @Column(name = "estimated_meta_cost_per_message", nullable = false, precision = 19, scale = 6)
  public BigDecimal getEstimatedMetaCostPerMessage() {
    return estimatedMetaCostPerMessage;
  }

  public void setEstimatedMetaCostPerMessage(BigDecimal v) {
    estimatedMetaCostPerMessage = v;
  }

  @Column(name = "markup_percent", nullable = false, precision = 9, scale = 4)
  public BigDecimal getMarkupPercent() {
    return markupPercent;
  }

  public void setMarkupPercent(BigDecimal v) {
    markupPercent = v;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false, length = 20)
  public BillingEnums.AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(BillingEnums.AccountStatus v) {
    accountStatus = v;
  }

  @Transient
  public boolean isSuspended() {
    return BillingEnums.AccountStatus.SUSPENDED == accountStatus;
  }
}
