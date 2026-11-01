package models;

import java.util.List;

public class JobDescription {
    private String title;
    private String company;
    private List<String> requiredSkills;
    private List<String> responsibilities;
    private List<String> qualifications;



    public JobDescription(String title, String company, List<String> requiredSkills,
                          List<String> responsibilities, List<String> qualifications) {
        this.title = title;
        this.company = company;
        this.requiredSkills = requiredSkills;
        this.responsibilities = responsibilities;
        this.qualifications = qualifications;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getResponsibilities() { return responsibilities; }
    public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }

    public List<String> getQualifications() { return qualifications; }
    public void setQualifications(List<String> qualifications) { this.qualifications = qualifications; }

    @Override
    public String toString() {
        return "JobDescription{" +
                "title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", requiredSkills=" + requiredSkills +
                ", responsibilities=" + responsibilities +
                ", qualifications=" + qualifications +
                '}';
    }
}

