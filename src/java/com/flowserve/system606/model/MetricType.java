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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.DATE;

@Entity
@Table(name = "METRIC_TYPES")
public class MetricType extends BaseEntity<Long> implements Comparable<MetricType>, Serializable {

    private static final long serialVersionUID = -8382719960002472187L;
    private static final String DECIMAL_METRIC = "DecimalMetric";
    public static final String CURRENCY_METRIC = "CurrencyMetric";
    public static final String PACKAGE_PREFIX = "com.flowserve.system606.model.";
    public static final String OWNER_ENTITY_TYPE_CONTRACT = "Contract";
    public static final String OWNER_ENTITY_TYPE_POB = "POB";
    private static final String OWNER_ENTITY_TYPE_ALL = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "METRIC_TYPE_SEQ")
    @SequenceGenerator(name = "METRIC_TYPE_SEQ", sequenceName = "METRIC_TYPE_SEQ", allocationSize = 1)
    @Column(name = "METRIC_TYPE_ID")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "DIRECTION")
    private MetricDirection direction;
    @Column(name = "OWNER_ENTITY_TYPE")
    private String ownerEntityType;
    @Column(name = "METRIC_CLASS")
    private String inputClass;
    @Column(name = "NAME")
    private String name;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION", length = 700)
    private String description;
    @Column(name = "METRIC_CURRENCY_TYPE")
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

    public MetricType() {
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

    public String getMetricClass() {
        return inputClass;
    }

    public void setMetricClass(String inputClass) {
        this.inputClass = inputClass;
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

    public CurrencyType getMetricCurrencyType() {
        return inputCurrencyType;
    }

    public void setMetricCurrencyType(CurrencyType inputCurrencyType) {
        this.inputCurrencyType = inputCurrencyType;
    }

    @Override
    public int compareTo(MetricType o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetricType) {
            return this.name.equals(((MetricType) obj).getName());
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

    public boolean isCurrency() {
        return CURRENCY_METRIC.equals(this.getMetricClass());
    }

    public boolean isDecimal() {
        return DECIMAL_METRIC.equals(this.getMetricClass());
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

    public boolean isConvertible() {
        return convertible;
    }

    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }
}
