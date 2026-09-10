package org.pahappa.systems.client.converters;

import org.sers.webutils.model.security.Permission;
import org.sers.webutils.server.core.service.PermissionService;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("permissionConverter")
public class PermissionConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		if (arg2 == null || arg2.isEmpty())
			return null;
		return (Permission) ApplicationContextProvider.getBean(PermissionService.class)
				.getObjectById(arg2);
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object object) {
		if (object == null || object instanceof String)
			return null;

		return ((Permission) object).getId();
	}
}
