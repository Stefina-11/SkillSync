
package com.skillsync.skillsyncbackend.controller;

import com.skillsync.skillsyncbackend.model.JobPosting;
import com.skillsync.skillsyncbackend.model.Resume;
import com.skillsync.skillsyncbackend.repository.JobPostingRepository;
import com.skillsync.skillsyncbackend.service.JobScrapingService;
import com.skillsync.skillsyncbackend.service.ResumeService;
import com.skillsync.skillsyncbackend.service.SkillMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private ResumeService resumeService;

    @Autowired // Added Autowired for jobScrapingService
    private JobScrapingService jobScrapingService;

    @Autowired
    private SkillMatchingService skillMatchingService;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    // Recruiter endpoints: create, update, delete
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobPosting> createJob(@RequestBody JobPosting job) {
        JobPosting saved = jobPostingRepository.save(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobPosting> updateJob(@PathVariable Long id, @RequestBody JobPosting updated) {
        var opt = jobPostingRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        JobPosting existing = opt.get();
        existing.setTitle(updated.getTitle());
        existing.setCompany(updated.getCompany());
        existing.setDescription(updated.getDescription());
        existing.setSkills(updated.getSkills());
        existing.setUrl(updated.getUrl());
        existing.setLocation(updated.getLocation());
        existing.setJobType(updated.getJobType());
        existing.setSalary(updated.getSalary());
        jobPostingRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<?> deleteJobById(@PathVariable Long id) {
        if (!jobPostingRepository.existsById(id)) return ResponseEntity.notFound().build();
        jobPostingRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    @GetMapping
    public ResponseEntity<List<JobPosting>> getJobs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "jobType", required = false) String jobType,
            @RequestParam(value = "minSalary", required = false) Double minSalary
    ) {
        logger.info("Received request for getJobs with filters: keyword={}, location={}, jobType={}, minSalary={}",
                keyword, location, jobType, minSalary);
        try {
            List<JobPosting> filteredJobs = jobPostingRepository.findByFilters(keyword, location, jobType, minSalary);
            logger.info("Found {} job postings after filtering.", filteredJobs.size());
            return ResponseEntity.ok(filteredJobs);
        } catch (Exception e) {
            logger.error("Error fetching job postings with filters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // This endpoint is for scraping and saving jobs, not for general listing.
    // It should probably be moved to an Admin or dedicated scraping controller if not already.
    // For now, keeping it as is, but noting its distinct purpose.
    @PostMapping("/fetch")
    @PreAuthorize("hasRole('ADMIN')") // Assuming only admin can trigger scraping
    public ResponseEntity<List<JobPosting>> fetchJobs() {
        // This method should ideally be in a service and called by an admin endpoint
        // For now, it remains here as per existing structure.
        return ResponseEntity.ok(jobScrapingService.fetchAndSaveJobPostings());
    }

    @GetMapping("/{jobId}/match/{resumeId}")
    public ResponseEntity<SkillMatchingService.MatchResult> matchSkills(@PathVariable Long jobId, @PathVariable Long resumeId) {
        Optional<JobPosting> jobPostingOpt = jobPostingRepository.findById(jobId);
        Optional<Resume> resumeOpt = resumeService.getResume(resumeId);

        if (jobPostingOpt.isPresent() && resumeOpt.isPresent()) {
            JobPosting jobPosting = jobPostingOpt.get();
            Resume resume = resumeOpt.get();
            SkillMatchingService.MatchResult matchResult = skillMatchingService.matchSkills(resume.getSkills(), jobPosting.getSkills());
            return ResponseEntity.ok(matchResult);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
