package org.pahappa.systems.egoSms.views.exceptions;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * Installs the application exception handler while preserving the handler
 * supplied by JSF/PrimeFaces.
 */
public class ViewExpiredExceptionHandlerFactory extends ExceptionHandlerFactory {

    private final ExceptionHandlerFactory parent;

    public ViewExpiredExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new ViewExpiredExceptionHandler(parent.getExceptionHandler());
    }
}
