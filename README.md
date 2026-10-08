# egoSms WhatsApp Center

## Client-paid Meta billing

Each client keeps a payment method on their own WhatsApp Business Account and Meta bills that account directly. egoSms records only its separately disclosed platform service fee.

Configure the service fee in `.env`:

```properties
EGOSMS_SERVICE_FEE_BILLING_ENABLED=true
EGOSMS_SERVICE_FEE_CURRENCY=UGX
EGOSMS_MONTHLY_SUBSCRIPTION_FEE=50000
```

One subscription fee is recorded for each client in each calendar month when they open Billing or send through WhatsApp Center. Sending additional messages in the same month does not create more egoSms charges. The immutable ledger is available under **WhatsApp Center > Billing**. Meta messaging fees and taxes are deliberately excluded from this ledger.

Administrators manage the currency, monthly fee, and active/suspended state for every connected WhatsApp account from **Settings > Billing**. The environment values are used as safe defaults when a newly connected account first receives a billing subscription.
