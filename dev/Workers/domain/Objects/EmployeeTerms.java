package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.JobStatus.fullTime;
import static dev.Workers.domain.Enums.JobStatus.halfTime;
import static dev.Workers.domain.Enums.SalaryType.global;
import static dev.Workers.domain.Enums.SalaryType.hourly;
    /**
     * Represents employment terms of an employee.
     *
     * Includes:
     * - Job status (full-time / half-time)
     * - Salary type (hourly / global)
     * - Number of annual rest days
     * - Weekly day off chosen
     */
public class EmployeeTerms {
    private JobStatus jobStatus;
    private SalaryType salaryType;
    private int restDays;
    private DayOfWeek dayOff;

    public EmployeeTerms(JobStatus jobStatus, SalaryType salaryType, int restDays, DayOfWeek dayOff){
        this.jobStatus = jobStatus;
        this.salaryType = salaryType;
        this.restDays = restDays;
        this.dayOff = dayOff;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void changeJobStatus() {
        if (jobStatus == fullTime)
            jobStatus = halfTime;
        else
            jobStatus = fullTime;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public void changeSalaryType() {
        if (this.salaryType == hourly)
            salaryType = global;
        else
            salaryType = hourly;
    }

    public int getRestDays() {
        return restDays;
    }

    public void setRestDays(int restDays) {
        this.restDays = restDays;
    }

    public DayOfWeek getDayOff() {
        return dayOff;
    }

    public void setDayOff(DayOfWeek dayOff) {
        this.dayOff = dayOff;
    }

    @Override
    public String toString() {
        return "Job Status: " + jobStatus +
                "\nSalary Type: " + salaryType +
                "\nRest Days: " + restDays +
                "\nDay off: " + dayOff;
    }
}
