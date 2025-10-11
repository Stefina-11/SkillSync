
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

    @Autowired
    private JobScrapingService jobScrapingService;

    @Autowired
    private ResumeService resumeService;

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
        List<JobPosting> all = jobScrapingService.getAllJobPostings();
        List<JobPosting> filtered = all.stream().filter(j -> {
            boolean ok = true;
            if (keyword != null && !keyword.isBlank()) {
                String k = keyword.toLowerCase();
                ok &= (j.getTitle() != null && j.getTitle().toLowerCase().contains(k))
                        || (j.getCompany() != null && j.getCompany().toLowerCase().contains(k))
                        || (j.getDescription() != null && j.getDescription().toLowerCase().contains(k));
            }
            if (location != null && !location.isBlank()) {
                ok &= j.getLocation() != null && j.getLocation().toLowerCase().contains(location.toLowerCase());
            }
            if (jobType != null && !jobType.isBlank()) {
                ok &= j.getJobType() != null && j.getJobType().equalsIgnoreCase(jobType);
            }
            if (minSalary != null) {
                ok &= j.getSalary() != null && j.getSalary() >= minSalary;
            }
            return ok;
        }).toList();
        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/fetch")
    public ResponseEntity<List<JobPosting>> fetchJobs() {
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
