package com.flowserve.system606.view;

import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class UserSearch implements Serializable {

    private static final long serialVersionUID = -1438027991420003830L;
    List<User> users = new ArrayList<User>();
    @Inject
    private AdminService adminService;
    private String searchString = "";

    public void search() throws Exception {

        users = adminService.searchUsers(searchString);
        Collections.sort(users);
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
