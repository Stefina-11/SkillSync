package com.skillsync.skillsyncbackend.controller;

import com.skillsync.skillsyncbackend.model.Resume;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.service.ResumeService;
import com.skillsync.skillsyncbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        Resume resume = resumeService.uploadResume(file);
        return ResponseEntity.ok(Map.of("id", resume.getId(), "skills", resume.getSkills()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resume> getMyResume() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return resumeService.getResumeByUserId(currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/ats-check")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<Resume> performAtsCheck(@PathVariable Long id) {
        return resumeService.performAtsCheck(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/rate")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<Resume> rateResume(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer rating = payload.get("rating");
        if (rating == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return resumeService.updateRecruiterRating(id, rating)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'RECRUITER')")
    public ResponseEntity<Resume> getResume(@PathVariable Long id) {
        return resumeService.getResume(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
