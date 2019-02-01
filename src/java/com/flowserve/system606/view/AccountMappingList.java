/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Account;
import com.flowserve.system606.model.AccountMapping;
import com.flowserve.system606.service.JournalService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class AccountMappingList implements Serializable {

    @Inject
    private JournalService journalService;

    List<AccountMapping> list = new ArrayList<AccountMapping>();

    @PostConstruct
    public void init() {
        list = journalService.findAllAccountMappings();
    }

    public List<AccountMapping> getList() {
        return list;
    }

    public void setList(List<AccountMapping> list) {
        this.list = list;
    }
}
