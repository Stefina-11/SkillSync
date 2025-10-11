package com.skillsync.skillsyncbackend.service;

import com.skillsync.skillsyncbackend.model.Resume;
import com.skillsync.skillsyncbackend.repository.ResumeRepository;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.repository.UserRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository; // To get the current user

    private final Tika tika = new Tika();

    public Resume uploadResume(MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String content;
        try {
            content = tika.parseToString(file.getInputStream());
        } catch (TikaException e) {
            throw new IOException("Failed to parse file", e);
        }
        List<String> skills = extractSkills(content);
        // Check if a resume already exists for this user
        Optional<Resume> existingResume = resumeRepository.findByUserId(currentUser.getId());
        Resume resume;
        if (existingResume.isPresent()) {
            resume = existingResume.get();
            resume.setFilename(file.getOriginalFilename());
            resume.setContent(content);
            resume.setSkills(skills);
        } else {
            resume = new Resume(file.getOriginalFilename(), content, skills, currentUser.getId());
        }
        return resumeRepository.save(resume);
    }

    public Optional<Resume> getResume(Long id) {
        return resumeRepository.findById(id);
    }

    public Optional<Resume> getResumeByUserId(Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    private List<String> extractSkills(String text) {
        List<String> knownSkills = Arrays.asList(
                "java", "spring", "spring boot", "rest api", "sql", "maven",
                "docker", "kubernetes", "python", "javascript", "react", "svelte",
                "aws", "azure", "git", "linux"
        );

        String lower = text == null ? "" : text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String s : knownSkills) {
            if (lower.contains(s)) {
                found.add(capitalizeSkill(s));
            }
        }
        return found;
    }

    private String capitalizeSkill(String s) {
        if (s.equals("spring")) return "Spring";
        if (s.equals("spring boot")) return "Spring Boot";
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            if (parts[i].length() > 0) {
                sb.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }
}
