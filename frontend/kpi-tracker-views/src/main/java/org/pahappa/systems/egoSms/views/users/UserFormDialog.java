package org.pahappa.systems.egoSms.views.users;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.Getter;
import lombok.Setter;
import org.pahappa.systems.egoSms.security.HyperLinks;
import org.pahappa.systems.egoSms.views.dialogs.DialogForm;
import org.sers.webutils.model.Gender;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.model.security.Role;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.core.service.UserService;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

/**
 *
 */
@ManagedBean(name = "userFormDialog", eager = true)
@Getter
@Setter
@SessionScoped
public class UserFormDialog extends DialogForm<User> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UserFormDialog.class.getSimpleName());
    private UserService userService;

    private List<Gender> listOfGenders;
    private List<Role> databaseRoles;
    private Set<Role> userRoles;
    private boolean edit;

    @PostConstruct
    public void init() {
        this.userService = ApplicationContextProvider.getBean(UserService.class);
        this.listOfGenders = Arrays.asList(Gender.values());
        this.databaseRoles = userService.getRoles();
    }

    public UserFormDialog() {
        super(HyperLinks.USER_FORM_DIALOG, 700, 450);
    }

    @Override
    public void persist() throws ValidationFailedException {
        super.model.setRoles(userRoles);
        this.userService.saveUser(super.model);
    }

    @Override
    public void resetModal() {
        super.resetModal();
        super.model = new User();
        setEdit(false);
    }

    @Override
    public void setFormProperties() {
        super.setFormProperties();
        if(super.model != null)
            setEdit(true);
        this.userRoles = new HashSet<>(userService.getRoles(super.model, 0, 0));
    }
}
