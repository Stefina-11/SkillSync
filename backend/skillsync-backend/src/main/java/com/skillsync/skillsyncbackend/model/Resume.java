package com.skillsync.skillsyncbackend.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;

@Entity
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filename;
    private String content;
    @ElementCollection
    private List<String> skills;
    private Long userId; // Link to the User who uploaded the resume
    private Double atsScore; // ATS score for the resume
    private String atsFeedback; // Feedback from ATS check
    private Integer recruiterRating; // Recruiter's rating for the resume (e.g., 1-5)

    public Resume() {
    }

    public Resume(String filename, String content, List<String> skills, Long userId, Double atsScore, String atsFeedback, Integer recruiterRating) {
        this.filename = filename;
        this.content = content;
        this.skills = skills;
        this.userId = userId;
        this.atsScore = atsScore;
        this.atsFeedback = atsFeedback;
        this.recruiterRating = recruiterRating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Double atsScore) {
        this.atsScore = atsScore;
    }

    public String getAtsFeedback() {
        return atsFeedback;
    }

    public void setAtsFeedback(String atsFeedback) {
        this.atsFeedback = atsFeedback;
    }

    public Integer getRecruiterRating() {
        return recruiterRating;
    }

    public void setRecruiterRating(Integer recruiterRating) {
        this.recruiterRating = recruiterRating;
    }
}
