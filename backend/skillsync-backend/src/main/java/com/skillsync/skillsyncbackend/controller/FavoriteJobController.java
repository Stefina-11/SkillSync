package com.skillsync.skillsyncbackend.controller;

import com.skillsync.skillsyncbackend.model.FavoriteJob;
import com.skillsync.skillsyncbackend.model.JobPosting;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.repository.FavoriteJobRepository;
import com.skillsync.skillsyncbackend.repository.JobPostingRepository;
import com.skillsync.skillsyncbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteJobController {

    @Autowired
    private FavoriteJobRepository favoriteJobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @PostMapping("/toggle/{jobId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long jobId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");

        JobPosting job = jobPostingRepository.findById(jobId).orElse(null);
        if (job == null) return ResponseEntity.notFound().build();

        var existing = favoriteJobRepository.findByUserAndJobPosting(user, job);
        if (existing.isPresent()) {
            favoriteJobRepository.delete(existing.get());
            return ResponseEntity.ok().body(java.util.Map.of("favorite", false));
        } else {
            FavoriteJob fav = new FavoriteJob(user, job);
            favoriteJobRepository.save(fav);
            return ResponseEntity.ok().body(java.util.Map.of("favorite", true));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> listFavorites(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");

        List<FavoriteJob> favs = favoriteJobRepository.findByUser(user);
        var jobs = favs.stream().map(f -> f.getJobPosting()).collect(Collectors.toList());
        return ResponseEntity.ok(jobs);
    }
}
