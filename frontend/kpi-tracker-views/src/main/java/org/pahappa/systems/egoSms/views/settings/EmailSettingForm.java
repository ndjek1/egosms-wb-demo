package org.pahappa.systems.egoSms.views.settings;

import org.pahappa.systems.egoSms.core.services.ApplicationSettingsService;
import org.pahappa.systems.egoSms.models.settings.ApplicationSettings;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.client.views.presenters.WebFormView;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "emailSettingForm")
@ViewScoped
public class EmailSettingForm extends WebFormView<ApplicationSettings, EmailSettingForm, EmailSettingForm> {
    private static final long serialVersionUID = 1L;
    private ApplicationSettingsService emailSettingsService;

    @Override
    @PostConstruct
    public void beanInit() {
        this.emailSettingsService = ApplicationContextProvider.getBean(ApplicationSettingsService.class);
        if (this.emailSettingsService.getActiveApplicationSettings() != null) {
            super.model = this.emailSettingsService.getActiveApplicationSettings();
        } else {
            super.model = new ApplicationSettings();
        }
        super.resetModal();
    }

    @Override
    public void pageLoadInit() {
        if (this.emailSettingsService.getActiveApplicationSettings() != null) {
            super.model = this.emailSettingsService.getActiveApplicationSettings();
        } else {
            super.model = new ApplicationSettings();
        }
    }

    @Override
    public void persist() throws Exception {
        try{
            this.emailSettingsService.saveInstance(super.model);
            UiUtils.showMessageBox("Action Successful", "Email settings updated");
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

    public ApplicationSettingsService getEmailSettingsService() {
        return emailSettingsService;
    }

    public void setEmailSettingsService(ApplicationSettingsService emailSettingsService) {
        this.emailSettingsService = emailSettingsService;
    }
}
