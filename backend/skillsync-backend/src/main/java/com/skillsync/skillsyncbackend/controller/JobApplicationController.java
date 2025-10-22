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
import org.springframework.security.core.context.SecurityContextHolder; // Import for SecurityContextHolder

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

    @DeleteMapping("/{applicationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteApplication(@PathVariable Long applicationId, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Job application not found"));

        // Ensure the current user is the one who submitted this application
        if (!application.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden
        }

        jobApplicationRepository.delete(application);
        return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<List<JobApplication>> getApplicationsForJob(@PathVariable Long jobId, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));

        // Ensure the recruiter owns this job posting or is an admin
        if (jobPosting.getRecruiterId() == null || (!jobPosting.getRecruiterId().equals(currentUser.getId()) && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        List<JobApplication> applications = jobApplicationRepository.findByJobPosting(jobPosting);
        return ResponseEntity.ok(applications);
    }
}
