package com.flowserve.system606.servlet;

import com.onelogin.saml2.Auth;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * The urlPattern hack here is due to the original attempted approach of using a JSP to handle the SAML assertion. This was the approach taken by the OneLogin
 * sample. The problem is in Payara/Glassfish, the JSP was setting the response content type automatically. This prevented the redirect to dashboard. Since we
 * had already gone through the major hassle of setting up Flowserve ADFS with the JSP consumer, we now map over the top to use this servlet instead.
 *
 * @author kgraves
 */
@WebServlet(name = "SamlConsumerServlet", urlPatterns = {"/samlAssertionConsumer.jsp"})
public class SamlConsumerServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Auth auth = new Auth(request, response);
            auth.processResponse();
            if (auth.getNameId() == null && auth.getLastErrorReason() != null) {
                Logger.getLogger(SamlConsumerServlet.class.getName()).log(Level.INFO, "Last SAML error: " + auth.getLastErrorReason());
                Logger.getLogger(SamlConsumerServlet.class.getName()).log(Level.INFO, "Remote IP: " + request.getRemoteAddr());
                List<String> errors = auth.getErrors();

                if (!errors.isEmpty()) {
                    //out.println("<p>" + StringUtils.join(errors, ", ") + "</p>");
                    Logger.getLogger(SamlConsumerServlet.class.getName()).log(Level.INFO, StringUtils.join(errors, ", "));
                }
                // The intermittent ADFS error is making it back here to the postback URL, but nameId is null.  Need to forward to error screen
            }
            request.getSession().setAttribute("nameId", auth.getNameId());
            Logger.getLogger(SamlConsumerServlet.class.getName()).log(Level.INFO, "ADFS successful login nameId: " + auth.getNameId());
            if (!response.isCommitted()) {
                com.onelogin.saml2.servlet.ServletUtils.sendRedirect(response, "/rcs/dashboard.xhtml");
            }
        } catch (Exception e) {
            Logger.getLogger(SamlConsumerServlet.class.getName()).log(Level.SEVERE, "Exception from SAML consumer servlet: ", e);
        }
    }
}
