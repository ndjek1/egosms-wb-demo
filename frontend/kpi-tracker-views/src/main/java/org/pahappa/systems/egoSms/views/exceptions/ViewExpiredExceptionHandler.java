package org.pahappa.systems.egoSms.views.exceptions;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.io.IOException;
import java.util.Iterator;

/**
 * Recovers from an expired server-side JSF view by issuing a fresh GET for the
 * same page. This is especially important for polling pages: after a redeploy
 * their next AJAX request contains a view-state token that no longer exists.
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

    private final ExceptionHandler wrapped;

    public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents().iterator();
        while (events.hasNext()) {
            ExceptionQueuedEvent event = events.next();
            ExceptionQueuedEventContext eventContext = (ExceptionQueuedEventContext) event.getSource();
            ViewExpiredException expired = findViewExpiredException(eventContext.getException());
            if (expired == null) {
                continue;
            }

            events.remove();
            redirectToFreshView(expired);
            return;
        }

        wrapped.handle();
    }

    private ViewExpiredException findViewExpiredException(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof ViewExpiredException) {
                return (ViewExpiredException) current;
            }
            current = current.getCause();
        }
        return null;
    }

    private void redirectToFreshView(ViewExpiredException expired) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        String viewId = expired.getViewId();
        if (viewId == null || !viewId.startsWith("/")) {
            viewId = "/pages/dashboard/Dashboard.xhtml";
        }

        try {
            externalContext.redirect(externalContext.getRequestContextPath() + viewId);
            facesContext.responseComplete();
        } catch (IOException redirectFailure) {
            throw new FacesException("Could not reload the expired JSF view", redirectFailure);
        }
    }
}
