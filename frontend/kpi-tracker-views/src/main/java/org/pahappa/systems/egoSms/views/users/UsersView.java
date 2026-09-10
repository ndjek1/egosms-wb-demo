package org.pahappa.systems.egoSms.views.users;

import com.googlecode.genericdao.search.Search;
import lombok.Getter;
import lombok.Setter;
import org.pahappa.systems.egoSms.security.HyperLinks;
import org.pahappa.systems.egoSms.security.UiUtils;
import org.pahappa.systems.egoSms.utils.GeneralSearchUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.sers.webutils.client.views.presenters.PaginatedTableView;
import org.sers.webutils.client.views.presenters.ViewPath;
import org.sers.webutils.model.Gender;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.security.Role;
import org.sers.webutils.model.security.User;
import org.sers.webutils.model.utils.SearchField;
import org.sers.webutils.server.core.service.RoleService;
import org.sers.webutils.server.core.service.UserService;
import org.sers.webutils.server.core.service.excel.reports.ExcelReport;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean(name = "usersView")
@Getter
@Setter
@SessionScoped
@ViewPath(path = HyperLinks.USERS_VIEW)
public class UsersView extends PaginatedTableView<User, UsersView, UsersView> {

    private static final long serialVersionUID = 1L;
    private UserService userService;

    private RoleService roleService;

    private String searchTerm;
    private Search search;

    private int total;
    private int noMale;
    private int noFemale;
    private int noUnknown;

    private List<Gender> genders = new ArrayList<>();
    private Gender selectedGender;
    private Date createdFrom, createdTo;

    private User selectedUser;
    private List<Role> rolesList = new ArrayList<>();
    private Set<Role> selectedRolesList = new HashSet<>();
    private List<SearchField> searchFields, selectedSearchFields;

    @PostConstruct
    public void init() {
        userService = ApplicationContextProvider.getBean(UserService.class);
        roleService = ApplicationContextProvider.getBean(RoleService.class);

        this.rolesList = roleService.getRoles();
        this.genders = Arrays.asList(Gender.values());
        this.reloadFilterReset();
    }

    @Override
    public void reloadFromDB(int offset, int limit, Map<String, Object> filters) {
        super.setDataModels(userService.getUsers(GeneralSearchUtils.composeUsersSearch(searchFields, searchTerm, selectedGender, createdFrom, createdTo), offset, limit));
    }

    @Override
    public List<User> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        return getDataModels();
    }

    @Override
    public void reloadFilterReset() {
        this.searchFields = Arrays.asList(new SearchField("First Name", "firstName"),
                new SearchField("Last Name", "lastName"), new SearchField("Phone Number", "phoneNumber"),
                new SearchField("Username", "username"), new SearchField("Email Address", "emailAddress"));
        this.search = GeneralSearchUtils.composeUsersSearch(searchFields, searchTerm, selectedGender, createdFrom, createdTo);
        super.setTotalRecords(userService.countUsers(this.search));

        this.total = super.getTotalRecords();
        this.noMale = userService.countUsers(this.search.copy().addFilterEqual("gender", Gender.MALE));
        this.noFemale = userService.countUsers(this.search.copy().addFilterEqual("gender", Gender.FEMALE));
        this.noUnknown = userService.countUsers(this.search.copy().addFilterEqual("gender", Gender.UNKNOWN));
        try {
            super.reloadFilterReset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSelectedUser(User user) {
        try {
            userService.deleteUser(user);
            UiUtils.showMessageBox("Action successful", "User has been deactivated.");
        } catch (OperationFailedException ex) {
            UiUtils.ComposeFailure("Action failed", ex.getLocalizedMessage());
            Logger.getLogger(UsersView.class.getName()).log(Level.SEVERE, null, ex);
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
}
