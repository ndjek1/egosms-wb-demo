package org.pahappa.systems.client.converters;

import org.sers.webutils.model.Gender;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("genderConverter")
public class GenderConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {

		if (arg2.equalsIgnoreCase(Gender.MALE.getName())) {
			return Gender.MALE;
		}

		if (arg2.equalsIgnoreCase(Gender.FEMALE.getName())) {
			return Gender.FEMALE;
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object object) {
		if (object == null || object instanceof String)
			return null;
		return ((Gender) object).getName();
	}
}
