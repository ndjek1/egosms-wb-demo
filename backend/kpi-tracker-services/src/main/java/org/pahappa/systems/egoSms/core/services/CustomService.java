package org.pahappa.systems.egoSms.core.services;

import com.googlecode.genericdao.search.Search;
import java.util.List;
import org.sers.webutils.model.security.Permission;
import org.sers.webutils.model.security.Role;

public interface CustomService {
  List<Permission> getPermissions(Search search, int offset, int limit);

  List<Role> getRoles(Search search, int offset, int limit);

  int countRoles(Search search);

  void deleteRole(Role role);
}
