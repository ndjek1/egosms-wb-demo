package org.pahappa.systems.egoSms.views.users;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.Getter;
import lombok.Setter;
import org.pahappa.systems.egoSms.core.services.CustomService;
import org.pahappa.systems.egoSms.core.services.impl.CustomServiceImpl;
import org.pahappa.systems.egoSms.security.HyperLinks;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.sers.webutils.client.views.presenters.PaginatedTableView;
import org.sers.webutils.client.views.presenters.ViewPath;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.model.security.Permission;
import org.sers.webutils.model.security.Role;
import org.sers.webutils.model.utils.SearchField;
import org.sers.webutils.model.utils.SortField;
import org.sers.webutils.server.core.service.PermissionService;
import org.sers.webutils.server.core.service.RoleService;
import org.sers.webutils.server.core.service.excel.reports.ExcelReport;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;
import org.sers.webutils.server.shared.CustomLogger;
import org.sers.webutils.server.shared.CustomLogger.LogSeverity;

import com.google.common.collect.Sets;
import com.googlecode.genericdao.search.Search;

@ManagedBean(name = "rolesView")
@Getter
@Setter
@SessionScoped
@ViewPath(path = HyperLinks.ROLES_VIEW)
public class RolesView extends PaginatedTableView<Role, RolesView, RolesView> {

	private static final long serialVersionUID = 1L;
	private RoleService roleService;
	private PermissionService permissionService;
	private CustomService customService;

	private String searchTerm;
	private Role selectedRole;
	private Set<Permission> permissionsList = new HashSet<Permission>();
	private Set<Permission> selectedPermissionsList = new HashSet<Permission>();
	private Search search;
	private Date startDate, endDate;
	private List<SearchField> searchFields;
	private SortField selectedSortField = new SortField("dateCreated", "dateCreated", true);


	@PostConstruct
	public void init() {
		this.roleService = ApplicationContextProvider.getBean(RoleService.class);
		this.permissionService = ApplicationContextProvider.getBean(PermissionService.class);
		this.customService = ApplicationContextProvider.getBean(CustomService.class);

		this.permissionsList = Sets.newHashSet(customService.getPermissions(new Search().addSortAsc("name"), 0, 0));
		this.searchFields = Arrays.asList(
				new SearchField[] { new SearchField("Name", "name"), new SearchField("Description", "description") });
		super.setMaximumresultsPerpage(10);
		reloadFilterReset();
	}

	@Override
	public List<Role> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		return getDataModels();
	}

	@Override
	public void reloadFromDB(int offset, int limit, Map<String, Object> filters) {
		super.setDataModels(this.customService.getRoles(search, offset, limit));
	}

	@Override
	public void reloadFilterReset() {
		this.search = CustomServiceImpl.composeRoleSearch(searchFields, searchTerm, startDate, endDate,
				selectedSortField);
		super.setTotalRecords(customService.countRoles(search));
		CustomLogger.log(LogSeverity.LEVEL_DEBUG, "Total records " + super.getTotalRecords());
		try {
			super.reloadFilterReset();
		} catch (Exception e) {
			CustomLogger.log(LogSeverity.LEVEL_ERROR, e.getMessage());
		}
	}

	@Override
	public List<ExcelReport> getExcelReportModels() {
		return Collections.emptyList();
	}

	@Override
	public String getFileName() {
		return null;
	}

	public void saveSelectedRole() throws ValidationFailedException {
		System.err.println("------saving role---------" + this.selectedRole);
		try {
			this.selectedRole.setPermissions(this.selectedPermissionsList);
			roleService.saveRole(this.selectedRole);
			UiUtils.showMessageBox("Action Success!", "Role updated");
		} catch (Exception e) {
			e.printStackTrace();
			UiUtils.showMessageBox("Action Failed!", e.getMessage());
		}
	}

	public void deleteSelectedRole(Role role) {
		try {
			customService.deleteRole(role);
			UiUtils.showMessageBox("Action Success!", "Role deleted");
		} catch (Exception e) {
			e.printStackTrace();
			UiUtils.showMessageBox("Action Failed!", e.getMessage());
		}
	}

	public void loadSelectedRole(Role role) {
		if (role != null) {
			System.err.println("------fetched roles---------" + role.getPermissions());
			this.selectedRole = role;
			this.selectedPermissionsList = new HashSet<Permission>(role.getPermissions());
		} else {
			System.err.println("------New role---------");
			this.selectedPermissionsList = new HashSet<Permission>();
			this.selectedRole = new Role();
		}
	}
}
