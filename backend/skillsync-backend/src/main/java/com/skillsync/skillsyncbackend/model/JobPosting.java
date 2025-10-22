package com.skillsync.skillsyncbackend.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;

@Entity
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String company;
    private String description;
    @ElementCollection
    private List<String> skills;
    private String url;
    private String location;
    private String jobType;
    private Double salary;
    private Long recruiterId; // Link to the User who posted the job

    public JobPosting() {
    }

    public JobPosting(String title, String company, String description, List<String> skills, String url, Long recruiterId) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.skills = skills;
        this.url = url;
        this.recruiterId = recruiterId;
    }

    public JobPosting(String title, String company, String description, List<String> skills, String url, String location, String jobType, Double salary, Long recruiterId) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.skills = skills;
        this.url = url;
        this.location = location;
        this.jobType = jobType;
        this.salary = salary;
        this.recruiterId = recruiterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }
}
