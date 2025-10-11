package com.skillsync.skillsyncbackend.controller;

import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Attempting to fetch profile for user: {}", username);
        // Extra debug logging to help diagnose forbidden issues
        try {
            Object principal = authentication.getPrincipal();
            logger.debug("Authentication principal: {}", principal);
        } catch (Exception ex) {
            logger.debug("No authentication principal available: {}", ex.getMessage());
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.error("User not found for username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User profile not found."));
        }
        // construct response with extended fields
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("email", user.getEmail() != null ? user.getEmail() : "");
        resp.put("role", user.getRole());
        resp.put("fullName", user.getFullName());
        resp.put("phone", user.getPhone());
        resp.put("bio", user.getBio());
        resp.put("linkedin", user.getLinkedin());
        resp.put("avatarDataUrl", user.getAvatarDataUrl());
        resp.put("github", user.getGithub());
        resp.put("jobTitle", user.getJobTitle());
        resp.put("careerLevel", user.getCareerLevel());
        try {
            resp.put("experience", user.getExperienceJson() != null ? objectMapper.readValue(user.getExperienceJson(), new TypeReference<java.util.List<java.util.Map<String, Object>>>() {}) : java.util.List.of());
            resp.put("education", user.getEducationJson() != null ? objectMapper.readValue(user.getEducationJson(), new TypeReference<java.util.List<java.util.Map<String, Object>>>() {}) : java.util.List.of());
            resp.put("preferredLocations", user.getPreferredLocationsJson() != null ? objectMapper.readValue(user.getPreferredLocationsJson(), new TypeReference<java.util.List<String>>() {}) : java.util.List.of());
            resp.put("expectedSalaryRange", user.getExpectedSalaryRangeJson() != null ? objectMapper.readValue(user.getExpectedSalaryRangeJson(), new TypeReference<java.util.Map<String, Object>>() {}) : Map.of());
            resp.put("workType", user.getWorkType());
            resp.put("employmentType", user.getEmploymentType());
            resp.put("achievements", user.getAchievementsJson() != null ? objectMapper.readValue(user.getAchievementsJson(), new TypeReference<java.util.List<java.util.Map<String, Object>>>() {}) : java.util.List.of());
        } catch (Exception e) {
            logger.error("Error parsing JSON for user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error processing user profile data."));
        }
        return ResponseEntity.ok(resp);
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody Map<String, Object> req) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.error("User not found for username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User profile not found."));
        }

        try {
            if (req.containsKey("password")) {
                Object pw = req.get("password");
                if (pw instanceof String && !((String) pw).isBlank()) {
                    user.setPassword(passwordEncoder.encode((String) pw));
                }
            }
            if (req.containsKey("email")) {
                Object em = req.get("email");
                if (em instanceof String) user.setEmail((String) em);
            }
            if (req.containsKey("fullName")) user.setFullName((String) req.get("fullName"));
            if (req.containsKey("phone")) user.setPhone((String) req.get("phone"));
            if (req.containsKey("bio")) user.setBio((String) req.get("bio"));
            if (req.containsKey("linkedin")) user.setLinkedin((String) req.get("linkedin"));
            if (req.containsKey("avatarDataUrl")) user.setAvatarDataUrl((String) req.get("avatarDataUrl"));
            if (req.containsKey("github")) user.setGithub((String) req.get("github"));
            if (req.containsKey("jobTitle")) user.setJobTitle((String) req.get("jobTitle"));
            if (req.containsKey("careerLevel")) user.setCareerLevel((String) req.get("careerLevel"));
            if (req.containsKey("experience")) {
                Object experienceObj = req.get("experience");
                if (experienceObj instanceof java.util.List) {
                    user.setExperienceJson(objectMapper.writeValueAsString(experienceObj));
                } else {
                    logger.warn("Experience received in updateProfile is not a list: {}", experienceObj);
                    user.setExperienceJson(objectMapper.writeValueAsString(java.util.List.of()));
                }
            }
            if (req.containsKey("education")) {
                Object educationObj = req.get("education");
                if (educationObj instanceof java.util.List) {
                    user.setEducationJson(objectMapper.writeValueAsString(educationObj));
                } else {
                    logger.warn("Education received in updateProfile is not a list: {}", educationObj);
                    user.setEducationJson(objectMapper.writeValueAsString(java.util.List.of()));
                }
            }
            if (req.containsKey("preferredLocations")) {
                Object preferredLocationsObj = req.get("preferredLocations");
                if (preferredLocationsObj instanceof java.util.List) {
                    java.util.List<String> preferredLocationsList = ((java.util.List<?>) preferredLocationsObj).stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .collect(Collectors.toList());
                    user.setPreferredLocationsJson(objectMapper.writeValueAsString(preferredLocationsList));
                } else {
                    logger.warn("PreferredLocations received in updateProfile is not a list: {}", preferredLocationsObj);
                    user.setPreferredLocationsJson(objectMapper.writeValueAsString(java.util.List.of()));
                }
            }
            if (req.containsKey("expectedSalaryRange")) {
                Object expectedSalaryRangeObj = req.get("expectedSalaryRange");
                if (expectedSalaryRangeObj instanceof java.util.Map) {
                    user.setExpectedSalaryRangeJson(objectMapper.writeValueAsString(expectedSalaryRangeObj));
                } else {
                    logger.warn("ExpectedSalaryRange received in updateProfile is not a map: {}", expectedSalaryRangeObj);
                    user.setExpectedSalaryRangeJson(objectMapper.writeValueAsString(java.util.Map.of()));
                }
            }
            if (req.containsKey("workType")) user.setWorkType((String) req.get("workType"));
            if (req.containsKey("employmentType")) user.setEmploymentType((String) req.get("employmentType"));
            if (req.containsKey("achievements")) {
                Object achievementsObj = req.get("achievements");
                if (achievementsObj instanceof java.util.List) {
                    user.setAchievementsJson(objectMapper.writeValueAsString(achievementsObj));
                } else {
                    logger.warn("Achievements received in updateProfile is not a list: {}", achievementsObj);
                    user.setAchievementsJson(objectMapper.writeValueAsString(java.util.List.of()));
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to parse profile update payload: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid profile payload"));
        }
        userRepository.save(user);
        // return updated profile
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail() != null ? user.getEmail() : "",
            "role", user.getRole()
        ));
    }
}
