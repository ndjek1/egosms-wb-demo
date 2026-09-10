package org.pahappa.systems.egoSms.models.settings;

import org.sers.webutils.model.BaseEntity;

import javax.persistence.*;

/**
 * @author ttc
 * This model is meant to hold all varibales that contribute to the settings of the system
 */
@Entity
@Table(name = "application_settings")
@Inheritance(strategy = InheritanceType.JOINED)
public class ApplicationSettings extends BaseEntity {

    private String senderAddress, senderPassword, senderSmtpHost, senderSmtpPort, paymentAPIUsername,
            paymentAPIPassword, paymentAPIBaseUrl, bankDetails, airtelMerchantCode, mtnMerchantCode, airtelMomo, mtnMomo,
            googleMapsAPIKey;

    /**
     * @return the senderAddress
     */
    @Column(name = "sender_address", nullable = false)
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * @param senderAddress the senderAddress to set
     */
    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    /**
     * @return the senderPassword
     */
    @Column(name = "sender_password", nullable = false)
    public String getSenderPassword() {
        return senderPassword;
    }

    /**
     * @param senderPassword the senderPassword to set
     */
    public void setSenderPassword(String senderPassword) {
        this.senderPassword = senderPassword;
    }

    /**
     * @return the senderSmtpHost
     */
    @Column(name = "sender_host", nullable = false)
    public String getSenderSmtpHost() {
        return senderSmtpHost;
    }

    /**
     * @param senderSmtpHost the senderSmtpHost to set
     */
    public void setSenderSmtpHost(String senderSmtpHost) {
        this.senderSmtpHost = senderSmtpHost;
    }

    /**
     * @return the senderSmtpPort
     */
    @Column(name = "sender_port", nullable = false)
    public String getSenderSmtpPort() {
        return senderSmtpPort;
    }

    /**
     * @param senderSmtpPort the senderSmtpPort to set
     */
    public void setSenderSmtpPort(String senderSmtpPort) {
        this.senderSmtpPort = senderSmtpPort;
    }

    /**
     * @return the paymentAPIUsername
     */
    @Column(name = "payment_api_username")
    public String getPaymentAPIUsername() {
        return paymentAPIUsername;
    }

    /**
     * @param paymentAPIUsername the paymentAPIUsername to set
     */
    public void setPaymentAPIUsername(String paymentAPIUsername) {
        this.paymentAPIUsername = paymentAPIUsername;
    }

    /**
     * @return the paymentAPIPassword
     */
    @Column(name = "payment_api_password")
    public String getPaymentAPIPassword() {
        return paymentAPIPassword;
    }

    /**
     * @param paymentAPIPassword the paymentAPIPassword to set
     */
    public void setPaymentAPIPassword(String paymentAPIPassword) {
        this.paymentAPIPassword = paymentAPIPassword;
    }

    /**
     * @return the paymentAPIBaseUrl
     */
    @Column(name = "payment_api_base_url")
    public String getPaymentAPIBaseUrl() {
        return paymentAPIBaseUrl;
    }

    /**
     * @return the bankDetails
     */
    @Column(name = "bank_details")
    public String getBankDetails() {
        return bankDetails;
    }

    /**
     * @param bankDetails the bankDetails to set
     */
    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    /**
     * @return the airtelMerchantCode
     */
    @Column(name = "airtel_merchant_code")
    public String getAirtelMerchantCode() {
        return airtelMerchantCode;
    }

    /**
     * @param airtelMerchantCode the airtelMerchantCode to set
     */
    public void setAirtelMerchantCode(String airtelMerchantCode) {
        this.airtelMerchantCode = airtelMerchantCode;
    }

    /**
     * @return the mtnMerchantCode
     */
    @Column(name = "mtn_merhcant_code")
    public String getMtnMerchantCode() {
        return mtnMerchantCode;
    }

    /**
     * @param mtnMerchantCode the mtnMerchantCode to set
     */
    public void setMtnMerchantCode(String mtnMerchantCode) {
        this.mtnMerchantCode = mtnMerchantCode;
    }

    /**
     * @return the airtelMomo
     */
    @Column(name = "airtel_momo")
    public String getAirtelMomo() {
        return airtelMomo;
    }

    /**
     * @param airtelMomo the airtelMomo to set
     */
    public void setAirtelMomo(String airtelMomo) {
        this.airtelMomo = airtelMomo;
    }

    /**
     * @return the mtnMomo
     */
    @Column(name = "mtn_momo")
    public String getMtnMomo() {
        return mtnMomo;
    }

    /**
     * @param mtnMomo the mtnMomo to set
     */
    public void setMtnMomo(String mtnMomo) {
        this.mtnMomo = mtnMomo;
    }

    /**
     * @param paymentAPIBaseUrl the paymentAPIBaseUrl to set
     */
    public void setPaymentAPIBaseUrl(String paymentAPIBaseUrl) {
        this.paymentAPIBaseUrl = paymentAPIBaseUrl;
    }

    /**
     *
     * @return the googleMapsAPIKey
     */
    @Column(name = "google_maps_api_key")
    public String getGoogleMapsAPIKey() {
        return googleMapsAPIKey;
    }

    /**
     *
     * @param googleMapsAPIKey the googleMapsAPIKey to set
     */
    public void setGoogleMapsAPIKey(String googleMapsAPIKey) {
        this.googleMapsAPIKey = googleMapsAPIKey;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ApplicationSettings && (super.getId() != null)
                ? super.getId().equals(((ApplicationSettings) object).getId()) : (object == this);
    }

    @Override
    public int hashCode() {
        return super.getId() != null ? this.getClass().hashCode() + super.getId().hashCode() : super.hashCode();
    }
}
