package org.pahappa.systems.client.converters;

import org.sers.webutils.model.utils.SearchField;
import org.sers.webutils.model.utils.SortField;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("searchFieldConverter")
public class SearchFieldConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		if (arg2 == null || arg2.isEmpty())
			return null;
		return SortField.getSearchFieldById(arg2);
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object object) {
		if (object == null || object instanceof String) {
			return null;
		}
		SearchField searchField = ((SearchField) object);
		return searchField.getId();
	}
}
