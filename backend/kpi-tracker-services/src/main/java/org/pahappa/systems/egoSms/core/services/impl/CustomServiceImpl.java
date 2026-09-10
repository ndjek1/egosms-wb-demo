package org.pahappa.systems.egoSms.core.services.impl;

import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import org.apache.commons.lang.StringUtils;
import org.pahappa.systems.egoSms.core.dao.impl.BaseDAOImpl;
import org.pahappa.systems.egoSms.core.services.CustomService;
import org.pahappa.systems.egoSms.utils.GeneralSearchUtils;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.security.Permission;
import org.sers.webutils.model.security.Role;
import org.sers.webutils.model.utils.SearchField;
import org.sers.webutils.model.utils.SortField;
import org.sers.webutils.server.core.dao.PermissionDao;
import org.sers.webutils.server.core.dao.RoleDao;
import org.sers.webutils.server.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CustomServiceImpl extends BaseDAOImpl<Role> implements CustomService {

    @Autowired
    RoleDao roleDao;

    @Autowired
    PermissionDao permissionDao;

    @Override
    public List<Permission> getPermissions(Search search, int offset, int limit) {
        if (search == null) {
            search = new Search();
        }
        search.setFirstResult(offset);
        search.setMaxResults(limit);

        return permissionDao.search(search);
    }
    @Override
    public List<Role> getRoles(Search search, int offset, int limit) {
        if (search == null)
            search = new Search();
        search.addFilterEqual("recordStatus", RecordStatus.ACTIVE);
        search.setFirstResult(offset);
        search.setMaxResults(limit);
        return roleDao.search(search);
    }

    @Override
    public int countRoles(Search search) {
        if (search == null)
            search = new Search();
        search.addFilterEqual("recordStatus", RecordStatus.ACTIVE);
        return roleDao.count(search);
    }

    @Override
    public void deleteRole(Role role) {
        role.setRecordStatus(RecordStatus.DELETED);
        roleDao.delete(role);
    }

    public static final Search composeRoleSearch(List<SearchField> searchFields, String query, Date createdFrom,
                                                 Date createdTo, SortField sortField) {
        if (query == null) {
            query = "";
        }

        Search search = new Search();
        search.addFilterEqual("recordStatus", RecordStatus.ACTIVE);

        if (createdFrom != null) {
            search.addFilterGreaterOrEqual("dateCreated", DateUtils.getMinimumDate(createdFrom));
        }

        if (createdTo != null) {
            search.addFilterLessOrEqual("dateCreated", DateUtils.getMaximumDate(createdTo));
        }

        if (sortField != null) {
            search.addSort(sortField.getSort());
        }

        if (StringUtils.isNotBlank(query) && GeneralSearchUtils.searchTermSatisfiesQueryCriteria(query)) {
            ArrayList<Filter> filters = new ArrayList<Filter>();
            GeneralSearchUtils.generateSearchTerms(searchFields, query, filters);
            search.addFilterAnd(filters.toArray(new Filter[filters.size()]));
        }
        return search;
    }
}
