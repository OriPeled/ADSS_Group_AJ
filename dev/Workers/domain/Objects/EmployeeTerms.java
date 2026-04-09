package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;

import static dev.Workers.domain.Enums.JobStatus.fullTime;
import static dev.Workers.domain.Enums.JobStatus.halfTime;
import static dev.Workers.domain.Enums.SalaryType.global;
import static dev.Workers.domain.Enums.SalaryType.hourly;

public class EmployeeTerms {
    private JobStatus jobStatus;
    private SalaryType salaryType;
    private int restDays;

    public EmployeeTerms(String jobStatus, String salaryType, int restDays) {
        this.jobStatus = JobStatus.valueOf(jobStatus);
        this.salaryType = SalaryType.valueOf(salaryType);
        this.restDays = restDays;
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

    @Override
    public String toString() {
        return "Employee Terms" +
                "\n1. Job Status: " + jobStatus +
                "\n2. Salary Type: " + salaryType +
                "\n3. Rest Days: " + restDays;
    }
}
