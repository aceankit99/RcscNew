package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "COUNTRIES")
public class Country extends TransientMeasurable<String> implements Comparable<Country>, Serializable {

    @Id
    @Column(name = "COUNTRY_ID")
    private String id;
    @Column(name = "CODE")
    private String code;
    @Column(name = "NAME")
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "country", cascade = CascadeType.MERGE)
    private List<ReportingUnit> reportingUnit = new ArrayList<ReportingUnit>();

    public Country() {
    }

    public Country(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Country o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Country{"
                + "id='" + id + '\''
                + ", code='" + code + '\''
                + ", name='" + name + '\''
                + '}';
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ReportingUnit> getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(List<ReportingUnit> reportingUnit) {
        this.reportingUnit = reportingUnit;
    }
}
