package org.pahappa.systems.egoSms.views.users;

import com.google.common.collect.Sets;
import com.googlecode.genericdao.search.Search;
import lombok.Getter;
import lombok.Setter;
import org.pahappa.systems.egoSms.core.services.CustomService;
import org.pahappa.systems.egoSms.security.HyperLinks;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.pahappa.systems.egoSms.views.dialogs.DialogForm;
import org.sers.webutils.model.security.Permission;
import org.sers.webutils.model.security.Role;
import org.sers.webutils.server.core.service.PermissionService;
import org.sers.webutils.server.core.service.RoleService;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 */
@ManagedBean(name = "roleFormDialog", eager = true)
@Getter
@Setter
@SessionScoped
public class RoleFormDialog extends DialogForm<Role> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RoleFormDialog.class.getSimpleName());
    private RoleService roleService;
    private PermissionService permissionService;
    private CustomService customService;

    private Set<Permission> permissionsList = new HashSet<Permission>();
    private Set<Permission> selectedPermissionsList = new HashSet<Permission>();

    @PostConstruct
    public void init() {
        this.roleService = ApplicationContextProvider.getBean(RoleService.class);
        this.permissionService = ApplicationContextProvider.getBean(PermissionService.class);
        this.customService = ApplicationContextProvider.getBean(CustomService.class);

        this.permissionsList = Sets.newHashSet(customService.getPermissions(new Search().addSortAsc("name"), 0, 0));
    }

    public RoleFormDialog() {
        super(HyperLinks.ROLE_FORM_DIALOG, 700, 450);
    }

    @Override
    public void persist() {
        try {
            super.model.setPermissions(this.selectedPermissionsList);
            roleService.saveRole(super.model);
            UiUtils.showMessageBox("Action Success!", "Role updated");
        } catch (Exception e) {
            e.printStackTrace();
            UiUtils.showMessageBox("Action Failed!", e.getMessage());
        }
    }

    @Override
    public void resetModal() {
        super.resetModal();
        super.model = new Role();
    }

    @Override
    public void setFormProperties() {
        super.setFormProperties();
        if(super.model  != null)
            this.selectedPermissionsList = new HashSet<>(super.model.getPermissions());
    }
}
