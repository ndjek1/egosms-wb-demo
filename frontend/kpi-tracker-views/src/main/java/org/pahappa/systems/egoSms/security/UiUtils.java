/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pahappa.systems.egoSms.security;

import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;

public class UiUtils {
   
    public static void  showMessageBox(String title, String description){
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, title,description);
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }
    public static void  ComposeFailure(String title, String description){
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, title,description);
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }
}
