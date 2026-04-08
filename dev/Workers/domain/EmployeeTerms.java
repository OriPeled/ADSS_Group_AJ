package dev.Workers.domain;

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

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public int getRestDays() {
        return restDays;
    }

    public void setRestDays(int restDays) {
        this.restDays = restDays;
    }
}
