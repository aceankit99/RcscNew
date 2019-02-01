package com.flowserve.system606.model;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.DATE;

@Entity
@Table(name = "EVENT_TYPES")
public class EventType extends BaseEntity<Long> implements Comparable<EventType>, Serializable {

    private static final long serialVersionUID = -8383719960102472187L;
    private static final String CURRENCY_EVENT = "CurrencyEvent";
    public static final String PACKAGE_PREFIX = "com.flowserve.system606.model.";
    private static final String OWNER_ENTITY_TYPE_CONTRACT = "Contract";
    private static final String OWNER_ENTITY_TYPE_POB = "POB";
    private static final String OWNER_ENTITY_TYPE_ALL = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EVENT_TYPE_SEQ")
    @SequenceGenerator(name = "EVENT_TYPE_SEQ", sequenceName = "EVENT_TYPE_SEQ", allocationSize = 1)
    @Column(name = "EVENT_TYPE_ID")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "DIRECTION")
    private MetricDirection direction;
    @Column(name = "OWNER_ENTITY_TYPE")
    private String ownerEntityType;
    @Column(name = "EVENT_CLASS")
    private String eventClass;
    @Column(name = "NAME")
    private String name;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "EVENT_CURRENCY_TYPE")
    private CurrencyType inputCurrencyType;
    @Column(name = "EXCEL_SHEET")
    private String excelSheet;  // for reading from xlsx
    @Column(name = "EXCEL_COL")
    private String excelCol;
    @Column(name = "GROUP_NAME")
    private String groupName;  // for grouping tabs in calc review screens
    @Column(name = "GROUP_POSITION")
    private int groupPosition;
    @Temporal(DATE)
    @Column(name = "EFFECTIVE_FROM")
    private LocalDate effectiveFrom;
    @Temporal(DATE)
    @Column(name = "EFFECTIVE_TO")
    private LocalDate effectiveTo;
    @Column(name = "IS_ACTIVE")
    private boolean active;   // maybe redundant
    @Column(name = "IS_REQUIRED")
    private boolean required;   // maybe redundant
    @Column(name = "IS_CONVERTIBLE")
    private boolean convertible;   // maybe redundant

    @OneToOne
    @JoinColumn(name = "SL_ACCT_CR_ID")
    private Account creditAccount;

    @OneToOne
    @JoinColumn(name = "SL_ACCT_DR_ID")
    private Account debitAccount;

    public EventType() {
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExcelSheet() {
        return excelSheet;
    }

    public void setExcelSheet(String excelSheet) {
        this.excelSheet = excelSheet;
    }

    public String getExcelCol() {
        return excelCol;
    }

    public void setExcelCol(String excelCol) {
        this.excelCol = excelCol;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupPosition() {
        return groupPosition;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String inputClass) {
        this.eventClass = inputClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerEntityType() {
        return ownerEntityType;
    }

    public void setOwnerEntityType(String ownerEntityType) {
        this.ownerEntityType = ownerEntityType;
    }

    public CurrencyType getEventCurrencyType() {
        return inputCurrencyType;
    }

    public void setEventCurrencyType(CurrencyType inputCurrencyType) {
        this.inputCurrencyType = inputCurrencyType;
    }

    @Override
    public int compareTo(EventType o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventType) {
            return this.name.equals(((EventType) obj).getName());
        }
        return false;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricDirection getDirection() {
        return direction;
    }

    public void setDirection(MetricDirection direction) {
        this.direction = direction;
    }

    public boolean isCurrencyEvent() {
        return CURRENCY_EVENT.equals(this.getEventClass());
    }

    public boolean isContractLevel() {
        return OWNER_ENTITY_TYPE_CONTRACT.equals(this.ownerEntityType) || OWNER_ENTITY_TYPE_ALL.equals(this.ownerEntityType);
    }

    public boolean isPobLevel() {
        return OWNER_ENTITY_TYPE_POB.equals(this.ownerEntityType) || OWNER_ENTITY_TYPE_ALL.equals(this.ownerEntityType);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Account getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(Account creditAccount) {
        this.creditAccount = creditAccount;
    }

    public Account getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(Account debitAccount) {
        this.debitAccount = debitAccount;
    }

    public boolean isConvertible() {
        return convertible;
    }

    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }

}
