package com.flowserve.system606.view;

import com.onelogin.saml2.Auth;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Using direct jsp for this. Remove when login approach is confirmed to be working.
 *
 * @author kgraves
 */
@Named
@RequestScoped
public class SamlLogin {

    @Inject
    private SecurityContext securityContext;

    //@Inject
    private FacesContext facesContext;

    @PostConstruct
    public void samlRedirect() {

        try {
            Logger.getLogger(SamlLogin.class.getName()).log(Level.INFO, "Redirecting SAML");
            Auth auth = new Auth(getRequest(), getResponse());
            if (getRequest().getParameter("attrs") == null) {
                auth.login();
            } else {
                String returnTo = getRequest().getPathInfo();
                auth.login(returnTo);
            }
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            Logger.getLogger(SamlLogin.class.getName()).log(Level.INFO, "Errorr: ", e);
        }
    }

    private HttpServletResponse getResponse() {
        return (HttpServletResponse) facesContext.getExternalContext().getResponse();
    }

    private HttpServletRequest getRequest() {
        return (HttpServletRequest) facesContext.getExternalContext().getRequest();
    }

    public String getMessage() {
        Logger.getLogger(SamlLogin.class.getName()).log(Level.INFO, "getMessage()");
        return "Login...";
    }
}
