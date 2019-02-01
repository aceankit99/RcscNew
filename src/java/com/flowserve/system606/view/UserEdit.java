/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.controller.AdminController;
import com.flowserve.system606.model.User;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named
@ViewScoped
public class UserEdit implements Serializable {

    User editUser = new User();

    @Inject
    private WebSession webSession;
    @Inject
    private AdminController adminController;

    public UserEdit() {
    }

    @PostConstruct
    public void init() {
        editUser = webSession.getEditUser();
    }

    public User getEditUser() {
        return editUser;
    }

    public String addUpdateCondition() throws Exception {

        return this.editUser.getId() == null ? adminController.addUser(this.editUser) : adminController.updateUser(this.editUser);
    }

    public String cancelSave() {
        webSession.setEditUser(null);
        return "userSearch";
    }

}
