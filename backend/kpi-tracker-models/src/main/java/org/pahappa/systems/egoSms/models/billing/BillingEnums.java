package org.pahappa.systems.egoSms.models.billing;

public final class BillingEnums {
  private BillingEnums() {}

  public enum AccountStatus {
    ACTIVE,
    SUSPENDED
  }

  public enum InvoiceStatus {
    OPEN,
    PAID
  }
}
