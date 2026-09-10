package org.pahappa.systems.egoSms.views.dialogs;

import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;

public class MessageComposer {
    public static void info(String title, String description){
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, title,description);
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }
    public static void warn(String title, String description){
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, title,description);
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }

    public static void error(String title, String description){
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, title,description);
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }
}
