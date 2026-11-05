package org.pahappa.systems.client.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.sers.webutils.model.utils.SortField;

@FacesConverter("sortFieldConverter")
public class SortFieldConverter implements Converter {

  @Override
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
    if (arg2 == null || arg2.isEmpty()) return null;
    return SortField.getSearchFieldById(arg2);
  }

  @Override
  public String getAsString(FacesContext arg0, UIComponent arg1, Object object) {
    if (object == null || object instanceof String) {
      return null;
    }
    SortField sortField = ((SortField) object);
    return sortField.getId();
  }
}
