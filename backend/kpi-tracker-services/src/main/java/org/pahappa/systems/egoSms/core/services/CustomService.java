package org.pahappa.systems.egoSms.core.services;

import com.googlecode.genericdao.search.Search;
import org.sers.webutils.model.security.Permission;
import org.sers.webutils.model.security.Role;

import java.util.List;

public interface CustomService {
    List<Permission> getPermissions(Search search, int offset, int limit);
    List<Role> getRoles(Search search, int offset, int limit);
    int countRoles(Search search);
    void deleteRole(Role role);

}
