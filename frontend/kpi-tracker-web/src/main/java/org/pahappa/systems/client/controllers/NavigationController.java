package org.pahappa.systems.client.controllers;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.pahappa.systems.egoSms.security.HyperLinks;

/**
 * Central place for URLs to xhtml pages.
 * <p>
 * Restrict the name of the bean, in case the name of the class is changed,
 * references to the class can still access the right bean.
 * <p>
 * Its application scoped to ensure that only one instance of the bean exists in
 * the entire life cycle of the application.
 */
@ManagedBean(name = "navigationController")
@ApplicationScoped
public class NavigationController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String userForm = HyperLinks.USER_FORM;
    private String rolesView = HyperLinks.ROLES_VIEW;
    private String profileView = HyperLinks.PROFILE_VIEW;
    private String mailSettingForm = HyperLinks.MAIL_SETTING_FORM;
    private String loginForm = HyperLinks.LOGIN_FORM;
    private String homePage = HyperLinks.DASHBOARD;

    public String getUserForm() {
        return userForm;
    }

    public String getRolesView() {
        return rolesView;
    }

    public String getProfileView() {
        return profileView;
    }

    public String getMailSettingForm() {
        return mailSettingForm;
    }

    public String getLoginForm() {
        return loginForm;
    }

    public void setUserForm(String userForm) {
        this.userForm = userForm;
    }

    public void setRolesView(String rolesView) {
        this.rolesView = rolesView;
    }

    public void setProfileView(String profileView) {
        this.profileView = profileView;
    }

    public void setMailSettingForm(String mailSettingForm) {
        this.mailSettingForm = mailSettingForm;
    }

    public void setLoginForm(String loginForm) {
        this.loginForm = loginForm;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }
}
