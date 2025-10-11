package com.skillsync.skillsyncbackend.controller;

import com.skillsync.skillsyncbackend.model.JobApplication;
import com.skillsync.skillsyncbackend.model.JobPosting;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.repository.JobApplicationRepository;
import com.skillsync.skillsyncbackend.repository.JobPostingRepository;
import com.skillsync.skillsyncbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobPostingRepository jobPostingRepository;

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> applyToJob(@PathVariable Long jobId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        JobPosting job = jobPostingRepository.findById(jobId).orElse(null);
        if (user == null || job == null) return ResponseEntity.badRequest().build();
        JobApplication app = new JobApplication(user, job, "APPLIED");
        jobApplicationRepository.save(app);
        return ResponseEntity.ok(Map.of("message", "Application submitted"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyApplications(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.badRequest().build();
        List<JobApplication> apps = jobApplicationRepository.findByUser(user);
        return ResponseEntity.ok(apps);
    }
}