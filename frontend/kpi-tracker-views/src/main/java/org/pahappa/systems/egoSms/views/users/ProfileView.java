package org.pahappa.systems.egoSms.views.users;


import lombok.Getter;
import lombok.Setter;
import org.pahappa.systems.egoSms.security.HyperLinks;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.sers.webutils.client.views.presenters.ViewPath;
import org.sers.webutils.client.views.presenters.WebFormView;
import org.sers.webutils.model.Gender;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.core.service.UserService;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;
import org.sers.webutils.server.shared.CustomLogger;
import org.sers.webutils.server.shared.CustomLogger.LogSeverity;
import org.sers.webutils.server.shared.SharedAppData;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@ManagedBean
@Getter
@Setter
@SessionScoped
@ViewPath(path = HyperLinks.PROFILE_VIEW)
public class ProfileView extends WebFormView<User, ProfileView, ProfileView> {

    private static final long serialVersionUID = 1L;

    private List<Gender> listOfGenders;
    private String currentPassword, newPassword, confirmedPassword;
    private UserService userService;
    private String imageUrl;

    @Override
    @PostConstruct
    public void beanInit() {
        super.model = SharedAppData.getLoggedInUser();
        this.userService = ApplicationContextProvider.getBean(UserService.class);

        this.listOfGenders = new ArrayList<>();
        this.listOfGenders.addAll(Arrays.asList(Gender.values()));
    }

    @Override
    public void pageLoadInit() {
        if (super.model == null) {
            super.model = SharedAppData.getLoggedInUser();
        }
        CustomLogger.log(LogSeverity.LEVEL_DEBUG, "User " + super.model.getUsername());
    }

    @Override
    public void persist() throws Exception {
        this.userService.saveUser(super.getModel());
    }

    @Override
    public void resetModal() {
        super.model = SharedAppData.getLoggedInUser();
    }

    @Override
    public String getViewUrl() {
        return this.getViewPath();
    }

    public void updatePassword() {
        try {
            validatePasswords();
            this.model.setClearTextPassword(newPassword);
            userService.saveUser(this.model);
            UiUtils.ComposeFailure("Action success!", "Password updated.");
        } catch (ValidationFailedException ex) {
            UiUtils.ComposeFailure("Action failed!", ex.getLocalizedMessage());
        }

    }

    public void validatePasswords() throws ValidationFailedException {
        if (!this.confirmedPassword.equals(this.newPassword)) {
            throw new ValidationFailedException("New passwords dont match");
        }
    }
}
