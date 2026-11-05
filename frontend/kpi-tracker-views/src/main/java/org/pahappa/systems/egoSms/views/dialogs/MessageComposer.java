package org.pahappa.systems.egoSms.views.dialogs;

import javax.faces.application.FacesMessage;
import org.primefaces.PrimeFaces;

public class MessageComposer {
  public static void info(String title, String description) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, title, description);
    PrimeFaces.current().dialog().showMessageDynamic(message);
  }

  public static void warn(String title, String description) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, title, description);
    PrimeFaces.current().dialog().showMessageDynamic(message);
  }

  public static void error(String title, String description) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, title, description);
    PrimeFaces.current().dialog().showMessageDynamic(message);
  }
}
