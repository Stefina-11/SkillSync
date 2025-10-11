package com.skillsync.skillsyncbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    private String status; // e.g., APPLIED, REVIEWED, REJECTED, ACCEPTED

    public JobApplication() {}

    public JobApplication(User user, JobPosting jobPosting, String status) {
        this.user = user;
        this.jobPosting = jobPosting;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public JobPosting getJobPosting() { return jobPosting; }
    public void setJobPosting(JobPosting jobPosting) { this.jobPosting = jobPosting; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}