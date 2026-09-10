package org.pahappa.systems.egoSms.views.settings;

import org.pahappa.systems.egoSms.core.services.ApplicationSettingsService;
import org.pahappa.systems.egoSms.models.settings.ApplicationSettings;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.client.views.presenters.WebFormView;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "paymentsSettingForm")
@ViewScoped
public class PaymentsSettingForm extends WebFormView<ApplicationSettings, PaymentsSettingForm, PaymentsSettingForm> {
    private static final long serialVersionUID = 1L;
    private ApplicationSettingsService paymentsSettingsService;

    @Override
    @PostConstruct
    public void beanInit() {
        this.paymentsSettingsService = ApplicationContextProvider.getBean(ApplicationSettingsService.class);
        if (this.paymentsSettingsService.getActiveApplicationSettings() != null) {
            super.model = this.paymentsSettingsService.getActiveApplicationSettings();
        } else {
            super.model = new ApplicationSettings();
        }
        super.resetModal();
    }

    @Override
    public void pageLoadInit() {
        if (this.paymentsSettingsService.getActiveApplicationSettings() != null) {
            super.model = this.paymentsSettingsService.getActiveApplicationSettings();
        } else {
            super.model = new ApplicationSettings();
        }
    }

    @Override
    public void persist() throws Exception {
        try {
            this.paymentsSettingsService.saveInstance(super.model);
            UiUtils.showMessageBox("Action Successful", "Settings updated");
        } catch (Exception e) {
            UiUtils.ComposeFailure("Action Failed", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void resetModal() {
        super.resetModal();
        super.model = new ApplicationSettings();
    }

    @Override
    public void setFormProperties() {
        super.setFormProperties();
    }

    @Override
    public String getViewUrl() {
        return this.getViewPath();
    }

    public ApplicationSettingsService getPaymentsSettingsService() {
        return paymentsSettingsService;
    }

    public void setPaymentsSettingsService(ApplicationSettingsService paymentsSettingsService) {
        this.paymentsSettingsService = paymentsSettingsService;
    }
}
