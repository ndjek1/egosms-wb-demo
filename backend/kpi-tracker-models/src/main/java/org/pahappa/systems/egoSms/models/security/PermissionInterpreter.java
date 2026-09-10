package org.pahappa.systems.egoSms.models.security;

import org.sers.webutils.model.security.Permission;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PermissionInterpreter {

	public static final List<Permission> reflectivelyGetPermissions() {

		List<Permission> permissions = new ArrayList<Permission>();

		for (Field field : PermissionConstants.class.getFields()) {

			if (field.isAnnotationPresent(SystemPermission.class)) {
				SystemPermission permissionAnnotation = field.getAnnotation(SystemPermission.class);
				Permission permission = new Permission();
				permission.setName(permissionAnnotation.name());
				permission.setDescription(permissionAnnotation.description());
				permissions.add(permission);
			}
		}
		return permissions;
	}
}
