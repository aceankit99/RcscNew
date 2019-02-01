package com.flowserve.system606.view;

import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.web.WebSession;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ApplicationScoped
public class CustomAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final String DASHBOARD = "dashboard";
    private static final String LOGIN = "login";
    private static final String RCS = "/rcs/";
    private static final String NAME_ID = "nameId";

    @Inject
    private AdminService adminService;
    @Inject
    private WebSession webSession;
    private boolean production = false;

    @PostConstruct
    public void init() {
        // Property is set via Payara - server-config - System Properties
        String env = System.getProperty("rcs_environment") == null ? "" : System.getProperty("rcs_environment");
        if ("Prod".equals(env)) {
            production = true;
        }
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {

        String requestURI = request.getRequestURI();

        try {
            if (webSession.getUser() == null && !production) {
                return executeNonProdAutoAdminLogin();
            }

            if (webSession.getUser() == null) {
                if (executeSamlAdfsFlowserveLogin(requestURI, request, response, httpMessageContext)) {
                    return AuthenticationStatus.SUCCESS;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CustomAuthenticationMechanism.class.getName()).log(Level.SEVERE, "Send to error page.", e);
        }

        return httpMessageContext.doNothing();
    }

    private AuthenticationStatus executeNonProdAutoAdminLogin() {
        Logger.getLogger(CustomAuthenticationMechanism.class.getName()).log(Level.INFO, "Non-production environment.  Bypassing ADFS login.  Using rcs_admin.");
        webSession.setUser(adminService.findUserByFlsId("rcs_admin"));
        return AuthenticationStatus.SUCCESS;
    }

    /**
     * After ADFS login, Flowserve ADFS will post back to URL samlAssertionConsumer.jsp, mapped by com.flowserve.system606.servlet.SamlConsumerServlet.java
     */
    private boolean executeSamlAdfsFlowserveLogin(String requestURI, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws IOException, Error, SettingsException {
        if (requestURI != null && (requestURI.contains(DASHBOARD) || requestURI.contains(LOGIN) || requestURI.equals(RCS))) {
            if (request.getSession().getAttribute(NAME_ID) == null) {
                Logger.getLogger(CustomAuthenticationMechanism.class.getName()).log(Level.INFO, "CustomAuthenticationMechanism.  Redirecting to ADFS login.");
                if (!response.isCommitted()) {
                    response.sendRedirect("https://adfs.flowserve.com/adfs/ls/IdpInitiatedSignOn.aspx?loginToRp=https://enspir.net");
//                    Auth auth = new Auth(request, response);
//                    auth.login();
                }
            } else {
                String userId = request.getSession().getAttribute(NAME_ID).toString();
                User user = adminService.findUserByFlsId(userId);
                webSession.setUser(user);
                Logger.getLogger(CustomAuthenticationMechanism.class.getName()).log(Level.INFO, "ADFS Login successful.  nameId: " + userId);
                httpMessageContext.notifyContainerAboutLogin(user, null);
                return true;
            }
        }
        return false;
    }
}
