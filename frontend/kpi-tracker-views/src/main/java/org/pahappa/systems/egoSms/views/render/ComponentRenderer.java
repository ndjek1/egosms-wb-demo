package org.pahappa.systems.egoSms.views.render;

import lombok.Getter;
import lombok.Setter;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.shared.SharedAppData;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@Getter
@Setter
@ManagedBean(name = "componentRenderer")
@SessionScoped
public class ComponentRenderer implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean administrator = false;
    private User loggedInUser;

    @PostConstruct
    public void init() {
        this.loggedInUser = SharedAppData.getLoggedInUser();

        if (this.loggedInUser != null)
            this.administrator = loggedInUser.hasAdministrativePrivileges();
    }
}