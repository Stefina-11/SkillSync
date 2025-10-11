package com.skillsync.skillsyncbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    // Extended profile fields
    private String fullName;
    private String phone;
    @Column(columnDefinition = "TEXT")
    private String bio;
    private String linkedin;
    @Column(columnDefinition = "TEXT")
    private String avatarDataUrl; // store data-url or small base64 string

    private String jobTitle;
    private String careerLevel;
    private String github;
    @Column(columnDefinition = "TEXT")
    private String experienceJson;
    @Column(columnDefinition = "TEXT")
    private String educationJson;
    @Column(columnDefinition = "TEXT")
    private String preferredLocationsJson;
    @Column(columnDefinition = "TEXT")
    private String expectedSalaryRangeJson;
    private String workType;
    private String employmentType;
    @Column(columnDefinition = "TEXT")
    private String achievementsJson;

    @Column(nullable = false)
    private String role; // e.g., ROLE_USER, ROLE_ADMIN, ROLE_RECRUITER

    public User() {}

    public User(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getAvatarDataUrl() { return avatarDataUrl; }
    public void setAvatarDataUrl(String avatarDataUrl) { this.avatarDataUrl = avatarDataUrl; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCareerLevel() { return careerLevel; }
    public void setCareerLevel(String careerLevel) { this.careerLevel = careerLevel; }

    public String getExperienceJson() { return experienceJson; }
    public void setExperienceJson(String experienceJson) { this.experienceJson = experienceJson; }

    public String getEducationJson() { return educationJson; }
    public void setEducationJson(String educationJson) { this.educationJson = educationJson; }

    public String getPreferredLocationsJson() { return preferredLocationsJson; }
    public void setPreferredLocationsJson(String preferredLocationsJson) { this.preferredLocationsJson = preferredLocationsJson; }

    public String getExpectedSalaryRangeJson() { return expectedSalaryRangeJson; }
    public void setExpectedSalaryRangeJson(String expectedSalaryRangeJson) { this.expectedSalaryRangeJson = expectedSalaryRangeJson; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getAchievementsJson() { return achievementsJson; }
    public void setAchievementsJson(String achievementsJson) { this.achievementsJson = achievementsJson; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
