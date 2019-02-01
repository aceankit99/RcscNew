package com.flowserve.system606.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity

@Table(name = "SYSTEM_USERS")
public class User implements Principal, Comparable<User>, Serializable {

    private static final long serialVersionUID = -4257640938927294079L;
    private static final Logger LOG = Logger.getLogger(User.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ")
    @SequenceGenerator(name = "USER_SEQ", sequenceName = "USER_SEQ", allocationSize = 50)
    @Column(name = "USER_ID")
    private Long id;
    @Column(name = "FLS_ID", nullable = false, length = 30, unique = true)
    private String flsId;
    @Column(name = "ALT_FLS_ID", length = 30)
    private String alternateFlsId;
    @Column(nullable = false, length = 512)
    private String name;
    @Column(name = "DISPLAY_NAME", length = 512)
    private String displayName;
    @Column(name = "IS_EMAILABLE")
    private boolean emailable;
    @Column(name = "EMAIL_ADDRESS", length = 512)
    private String emailAddress;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPERVISOR_ID")
    private User supervisor;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DELEGATE_ID")
    private User delegate;
    @Column(name = "OFFICE_NAME")
    private String officeName;
    @Column(name = "COMMON_NAME_LDAP")
    private String commonNameLDAP;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "ORG_LEVEL")
    private int orgLevel;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY_ID")
    private Country country;
    @Column(name = "LOCALE")
    private String locale;
    @Column(name = "IS_ADMIN")
    private boolean admin;
    @Column(name = "GlOBAL_VIEWER")
    private boolean globalViewer;

    public User(String flsId, String name, String displayName, String emailAddress) {
        this.flsId = flsId;
        this.name = name;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
    }

    public User(String flsId, String displayName, String commonNameLDAP, String emailAddress, String officeName, String title, int orgLevel) {
        this.flsId = flsId;
        this.name = displayName;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.officeName = officeName;
        this.commonNameLDAP = commonNameLDAP;
        this.title = title;
        this.orgLevel = orgLevel;
    }

    @Override
    public int compareTo(User obj) {
        return this.name.compareTo(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return this.id.equals(((User) obj).getId());
        }
        return false;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlsId() {
        return flsId;
    }

    public void setFlsId(String flsId) {
        this.flsId = flsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamePlusOffice() {
        if (officeName != null) {
            return name + " - " + officeName;
        }

        return name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAlternateFlsId() {
        return alternateFlsId;
    }

    public void setAlternateFlsId(String alternateFlsId) {
        this.alternateFlsId = alternateFlsId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEmailable() {
        return emailable;
    }

    public void setEmailable(boolean emailable) {
        this.emailable = emailable;
    }

    public User getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }

    public User getDelegate() {
        return delegate;
    }

    public void setDelegate(User delegate) {
        this.delegate = delegate;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getCommonNameLDAP() {
        return commonNameLDAP;
    }

    public void setCommonNameLDAP(String commonNameLDAP) {
        this.commonNameLDAP = commonNameLDAP;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrgLevel() {
        return orgLevel;
    }

    public void setOrgLevel(int orgLevel) {
        this.orgLevel = orgLevel;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isGlobalViewer() {
        return globalViewer;
    }

    public void setGlobalViewer(boolean globalViewer) {
        this.globalViewer = globalViewer;
    }

}
