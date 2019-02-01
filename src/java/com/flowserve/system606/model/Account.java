/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ACCOUNTS")
public class Account extends BaseEntity<String> implements Serializable {

    @Id
    private String id;
    @Column(name = "ACCT_NAME")
    private String name;
    @Column(name = "ACCT_DESC")
    private String description;
    @Column(name = "ACCT_TYPE")
    private AccountType accountType;
    @OneToOne
    @JoinColumn(name = "COMPANY_ID")
    private Company company;
    @OneToOne
    @JoinColumn(name = "OFFSET_ACCOUNT_ID")
    private Account offsetAccount;

    public Account() {
    }

    public boolean isDebitAccount() {
        return accountType == AccountType.DEBIT;
    }

    public boolean isCreditAccount() {
        return accountType == AccountType.CREDIT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Account getOffsetAccount() {
        return offsetAccount;
    }

    public void setOffsetAccount(Account offsetAccount) {
        this.offsetAccount = offsetAccount;
    }

    public boolean isDebit() {
        return this.accountType.equals(AccountType.DEBIT);
    }

    public boolean isCredit() {
        return this.accountType.equals(AccountType.CREDIT);
    }
}
