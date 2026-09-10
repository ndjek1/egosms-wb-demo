package org.pahappa.systems.egoSms.views.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.Getter;
import lombok.Setter;
import org.pahappa.systems.egoSms.security.HyperLinks;
import org.sers.webutils.client.views.presenters.ViewPath;
import org.sers.webutils.client.views.presenters.WebFormView;
import org.sers.webutils.model.Gender;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.security.Role;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.core.service.UserService;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;
import org.sers.webutils.server.core.utils.TelephoneNumberUtils;
import org.springframework.beans.BeansException;

@ManagedBean
@Getter
@Setter
@SessionScoped
@ViewPath(path = HyperLinks.USER_FORM)
public class UserForm extends WebFormView<User, UserForm, UsersView> {

	private static final long serialVersionUID = 1L;
	private List<Gender> listOfGenders;
	private UserService userService;
	private List<Role> databaseRoles, userRoles;

	@Override
	@PostConstruct
	public void beanInit() {
		this.userService = ApplicationContextProvider.getBean(UserService.class);
		this.listOfGenders = new ArrayList<Gender>();
		this.listOfGenders.addAll(Arrays.asList(Gender.values()));
	}

	@Override
	public void pageLoadInit() {
		this.databaseRoles = userService.getRoles();
	}

	@Override
	public void persist() throws Exception {
		super.getModel().setRoles(new HashSet<Role>());
		for (Role role : userRoles)
			super.getModel().addRole(role);

		super.getModel().addRole(userService.getRoleByRoleName(Role.DEFAULT_WEB_ACCESS_ROLE));
		super.model.setPhoneNumber(TelephoneNumberUtils.getValidTelephoneNumber(super.model.getPhoneNumber()));
		userService.saveUser(super.getModel());
	}

	@Override
	public void resetModal() {
		super.resetModal();
		super.model = new User();
		this.userRoles = new ArrayList<Role>();
	}

	@Override
	public void setFormProperties() {
		super.setFormProperties();
		this.userRoles = new ArrayList<Role>(super.getModel().getRoles());
	}

	public void deleteUser(User user) throws BeansException, OperationFailedException {
		userService.deleteUser(user);
	}
}
