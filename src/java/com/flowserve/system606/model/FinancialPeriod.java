/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;
import javax.persistence.Transient;

@Entity
@Table(name = "FINANCIAL_PERIODS")
public class FinancialPeriod extends BaseEntity<String> implements Comparable<FinancialPeriod>, Serializable {

    private static final long serialVersionUID = 8354236373683763082L;
    private static final Logger LOG = Logger.getLogger(FinancialPeriod.class.getName());
    @Id
    @Column(name = "PERIOD_ID")
    private String id;
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERIOD_SEQ")
    @SequenceGenerator(name = "PERIOD_SEQ", sequenceName = "PERIOD_SEQ", allocationSize = 50)
    @Column(name = "PERIOD_SEQ")
    private Long sequence;
    @Column(name = "NAME")
    private String name;
    @Column(name = "START_DATE")
    private LocalDate startDate;
    @Column(name = "END_DATE")
    private LocalDate endDate;
    @Column(name = "PERIOD_YEAR")
    private int periodYear;
    @Column(name = "PERIOD_MONTH")  // TODO - Change to comparable int
    private Integer periodMonth;
    @Column(name = "STATUS")
    private PeriodStatus status;
    @OneToOne
    @JoinColumn(name = "CREATED_BY_ID")
    private User createdBy;
    @Temporal(TIMESTAMP)
    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;
    @OneToOne
    @JoinColumn(name = "LAST_UPDATED_BY_ID")
    private User lastUpdatedBy;
    @Temporal(TIMESTAMP)
    @Column(name = "LAST_UPDATE_DATE")
    private LocalDateTime lastUpdateDate;
    @OneToOne
    @JoinColumn(name = "LOCAL_CURRENCY_RATE_PERIOD")
    private FinancialPeriod localCurrencyRatePeriod;
    @OneToOne
    @JoinColumn(name = "REPORTING_CURRENCY_RATE_PERIOD")
    private FinancialPeriod reportingCurrencyRatePeriod;
    @OneToOne
    @JoinColumn(name = "NEXT_PERIOD_ID")
    private FinancialPeriod nextPeriod;
    @OneToOne
    @JoinColumn(name = "PRIOR_PERIOD_ID")
    private FinancialPeriod priorPeriod;

    @Transient
    private final LocalDate novemberFirst2017;

    public FinancialPeriod() {
        novemberFirst2017 = LocalDate.of(2017, Month.NOVEMBER, 1);
    }

    public FinancialPeriod(String id, String name, LocalDate startDate, LocalDate endDate, int periodYear, int periodMonth, PeriodStatus status) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.status = status;
        this.novemberFirst2017 = LocalDate.of(2017, Month.NOVEMBER, 1);
    }

//    public boolean isBetween(LocalDate startDate, LocalDate endDate) {
//        if (startDate == null || endDate == null) {
//            return false;
//        }
//
//        if (this.startDate.withDayOfMonth(15).isAfter(startDate.withDayOfMonth(1))
//                && this.startDate.withDayOfMonth(15).isBefore(endDate.withDayOfMonth(25))) {
//            return true;
//        }
//
//        return false;
//    }
    public boolean isDateWithinPeriodOrPriorToNov2017(LocalDate date) {
        if (date == null) {
            return false;
        }

        if (date.isBefore(novemberFirst2017)) {
            return true;
        }

        if (date.isEqual(this.startDate) || date.isEqual(this.endDate)) {
            return true;
        }

        if (date.isAfter(this.startDate) && date.isBefore(this.endDate)) {
            return true;
        }

        return false;
    }

    public boolean isBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        if ((this.endDate.equals(startDate) || this.endDate.isAfter(startDate)) && (this.endDate.equals(endDate) || this.endDate.isBefore(endDate))) {
            return true;
        }

        return false;
    }

//    public int getPeriodMonthDuration(LocalDate startDate, LocalDate endDate) {
//        if (!this.isBetween(startDate, endDate)) {
//            return 0;
//        }
//
//        if (startDate == null || endDate == null) {
//            return 0;
//        }
//
//        if (startDate.isAfter(endDate)) {
//            return 0;
//        }
//
//        if (this.startDate.withDayOfMonth(1).isBefore(endDate.withDayOfMonth(1))) {
//            endDate = this.startDate.withDayOfMonth(1);
//        }
//
//        Period diff = Period.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1));
//
//        return diff.getMonths();
//    }
//    public float getStraightLineRevenueAllocationFactor(LocalDate slStartDate, LocalDate slEndDate) {
//        if (slStartDate == null || slEndDate == null) {
//            return 0.0f;
//        }
//
//        slStartDate = slStartDate.withDayOfMonth(1);
//        slEndDate = slEndDate.withDayOfMonth(1);
//
//        if (this.startDate.isBefore(slStartDate)) {
//            return 0.0f;
//        }
//
//        if (this.startDate.isAfter(slEndDate) || this.startDate.isEqual(slEndDate)) {
//            return 1.0f;
//        }
//
//        if (this.isBetween(slStartDate, slEndDate)) {
//            /**
//             * We have verified that we are within the window in which we can calculate revenue. In order to account for the very first month, we must adjust
//             * the starting month one month backward to allow a delta of 1-month for the first month.
//             */
//            slStartDate = slStartDate.minus(1, ChronoUnit.MONTHS);
//            float factor = ((float) ChronoUnit.MONTHS.between(this.startDate, slStartDate)) / ((float) ChronoUnit.MONTHS.between(slEndDate, slStartDate));
//            return factor;
//        }
//
//        return 0.0f;
//    }
//
    public float getStraightLineRevenueAllocationFactor(LocalDate slStartDate, LocalDate slEndDate) {
        if (slStartDate == null || slEndDate == null) {
            return 0.0f;
        }

        if (this.endDate.isBefore(slStartDate)) {
            return 0.0f;
        }

        if (this.endDate.isAfter(slEndDate) || this.endDate.isEqual(slEndDate)) {
            return 1.0f;
        }

        if (this.isBetween(slStartDate, slEndDate)) {
            // KJG 10/9/2018 - Add an extra day to these to account for a full end date.  Plus prevents potential divide by zero.
            float daysBetweenPeriodEndDateAndSLStartDate = (float) (ChronoUnit.DAYS.between(slStartDate, this.endDate) + 1);
            float daysBetweenSLEndDateAndSLStartDate = (float) (ChronoUnit.DAYS.between(slStartDate, slEndDate) + 1);
            float factor = daysBetweenPeriodEndDateAndSLStartDate / daysBetweenSLEndDateAndSLStartDate;

            return factor;
        }

        return 0.0f;
    }

    @Override
    public int compareTo(FinancialPeriod obj) {
        return this.endDate.compareTo(obj.getEndDate());
    }

    public boolean isAfter(FinancialPeriod period) {
        return this.endDate.compareTo(period.getEndDate()) > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(int periodYear) {
        this.periodYear = periodYear;
    }

    public PeriodStatus getStatus() {
        return status;
    }

    public void setStatus(PeriodStatus status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(Integer periodMonth) {
        this.periodMonth = periodMonth;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public boolean isOpen() {
        if (this.status == null) {
            return false;
        }

        return this.status.equals(status.OPENED);
    }

    public boolean isClosed() {
        if (this.status == null) {
            return false;
        }

        return this.status.equals(status.CLOSED);
    }

    public boolean isUserFreeze() {
        if (this.status == null) {
            return false;
        }

        return this.status.equals(status.USER_FREEZE);
    }

    public boolean isNeverOpened() {
        if (this.status == null) {
            return false;
        }

        return this.status.equals(status.NEVER_OPENED);
    }

    public FinancialPeriod getLocalCurrencyRatePeriod() {
        return localCurrencyRatePeriod;
    }

    public void setLocalCurrencyRatePeriod(FinancialPeriod localCurrencyRatePeriod) {
        this.localCurrencyRatePeriod = localCurrencyRatePeriod;
    }

    public FinancialPeriod getReportingCurrencyRatePeriod() {
        return reportingCurrencyRatePeriod;
    }

    public void setReportingCurrencyRatePeriod(FinancialPeriod reportingCurrencyRatePeriod) {
        this.reportingCurrencyRatePeriod = reportingCurrencyRatePeriod;
    }

    public FinancialPeriod getNextPeriod() {
        return nextPeriod;
    }

    public void setNextPeriod(FinancialPeriod nextPeriod) {
        this.nextPeriod = nextPeriod;
    }

    public FinancialPeriod getPriorPeriod() {
        return priorPeriod;
    }

    public void setPriorPeriod(FinancialPeriod priorPeriod) {
        this.priorPeriod = priorPeriod;
    }

}
