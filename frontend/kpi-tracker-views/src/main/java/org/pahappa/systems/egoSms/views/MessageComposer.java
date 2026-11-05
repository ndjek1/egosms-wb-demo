package org.pahappa.systems.egoSms.views;

import javax.faces.application.FacesMessage;
import org.primefaces.PrimeFaces;

public class MessageComposer {
  public static void compose(String title, String description) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, title, description);
    PrimeFaces.current().dialog().showMessageDynamic(message);
  }

  public static void failed(String title, String description) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, title, description);
    PrimeFaces.current().dialog().showMessageDynamic(message);
  }
}
