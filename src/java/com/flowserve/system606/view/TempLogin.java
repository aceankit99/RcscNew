package com.flowserve.system606.view;

import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author kgraves
 */
@Named
@ViewScoped
public class TempLogin implements Serializable {

    private static final Logger logger = Logger.getLogger(TempLogin.class.getName());

    @Inject
    private AdminService adminService;
    @Inject
    private WebSession webSession;
    @Inject
    private ViewSupport viewSupport;

    private String username;
    private String password;

    @PostConstruct
    public void init() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() throws Exception {
        if (username == null || !"Flowserve1".equals(password)) {
            return "login";
        }

        User user = adminService.findUserByFlsId(username);

        if (user == null) {
            //FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

            return "login?faces-redirect=true";
        }

        webSession.setUser(user);
        webSession.init();

        return "dashboard";
    }

}
