package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;

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
     * - Number of rest days per period
     */
public class EmployeeTerms {
    // employee job status (full job ,half job)
    private JobStatus jobStatus;
    // employee salary Type (per houer ,global)
    private SalaryType salaryType;
    // Number of rest days assigned to the employee
    private int restDays;


    /**
     * Constructor for EmployeeTerms with full validation logic.
     *  @param jobStatus   The status of the job (Full time / Half time)
     * @param salaryType  The way the employee is paid (Global / Hourly)
     * @param restDays    Number of weekly rest days (Must be between 1 and 6)
     * @throws IllegalArgumentException if any parameter is invalid.
     */
    public EmployeeTerms(JobStatus jobStatus, SalaryType salaryType, int restDays){

        this.jobStatus = jobStatus;
        this.salaryType = salaryType;
        this.restDays = restDays;
    }
    /**
    * @return job status (full-time / half-time)
    */
    public JobStatus getJobStatus() {
        return jobStatus;
    }
     /**
      * Updates job status
      * @param jobStatus new job status
      */
    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void changeJobStatus() {
        if (jobStatus == fullTime)
            jobStatus = halfTime;
        else
            jobStatus = fullTime;
    }
    /**
     * @return salary type (hourly / global)
     */
    public SalaryType getSalaryType() {
        return salaryType;
    }

    /**
     * Updates salary type
     * @param salaryType new salary type
     */
    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    /**
     * @return number of rest days
     */
    public void changeSalaryType() {
        if (this.salaryType == hourly)
            salaryType = global;
        else
            salaryType = hourly;
    }
    /**
     * @return number of rest days
     */
    public int getRestDays() {
        return restDays;
    }
    /**
     * Updates number of rest days
     * @param restDays new number of rest days
     */
    public void setRestDays(int restDays) {
        this.restDays = restDays;
    }
    /**
     * @return string representation of employee terms
     */
    @Override
    public String toString() {
        return "Employee Terms" +
                "\n1. Job Status: " + jobStatus +
                "\n2. Salary Type: " + salaryType +
                "\n3. Rest Days: " + restDays;
    }
}
