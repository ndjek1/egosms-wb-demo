package org.pahappa.systems.egoSms.security;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


/**
 * 
 */
@ManagedBean(name = "loginController")
@ViewScoped
public class LoginController implements PhaseListener {

	private static final long serialVersionUID = 1L;
	private static final String SPRING_SECURITY_CHECK_ACTION = "/j_spring_security_check";

	/**
	 * Redirect the login request directly to spring security check as stated in
	 * spring security configuration xml file
	 * 
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String doLogin() throws ServletException, IOException {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		RequestDispatcher dispatcher = ((ServletRequest) context.getRequest())
				.getRequestDispatcher(SPRING_SECURITY_CHECK_ACTION);
		dispatcher.forward((ServletRequest) context.getRequest(), (ServletResponse) context.getResponse());
		FacesContext.getCurrentInstance().responseComplete();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.event.PhaseListener#getPhaseId()
	 * 
	 * In which phase you want to interfere?
	 */
	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}

	public void afterPhase(PhaseEvent event) {
		// TODO Auto-generated method stub

	}

	public void beforePhase(PhaseEvent event) {
		// TODO Auto-generated method stub
	}
}