package models;

import java.util.List;
import java.util.Map;

public class CV {
    private String name;
    private String email;
    private String phone;
    private List<String> skills;
    private List<String> education;
    private List<String> experience;
    private String filePath;
    private double matchScore;
    // NLP-enhanced fields
    private Map<String, List<String>> entities;
    private List<String> lemmatizedText;

    public CV(String name, String email, String phone, List<String> skills,
              List<String> education, List<String> experience, String filePath) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.filePath = filePath;
    }

    public CV() {
        // Default constructor
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    // NLP-enhanced getters and setters
    public Map<String, List<String>> getEntities() { return entities; }
    public void setEntities(Map<String, List<String>> entities) { this.entities = entities; }

    public List<String> getLemmatizedText() { return lemmatizedText; }
    public void setLemmatizedText(List<String> lemmatizedText) {
        this.lemmatizedText = lemmatizedText;
    }

    @Override
    public String toString() {
        return "CV{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", skills=" + skills +
                ", education=" + education +
                ", experience=" + experience +
                ", filePath='" + filePath + '\'' +
                ", matchScore=" + matchScore +
                ", entities=" + entities +
                ", lemmatizedText=" + lemmatizedText +
                '}';
    }
}