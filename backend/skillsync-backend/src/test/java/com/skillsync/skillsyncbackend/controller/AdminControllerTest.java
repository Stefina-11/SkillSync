package com.skillsync.skillsyncbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.skillsyncbackend.model.JobPosting;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.repository.JobPostingRepository;
import com.skillsync.skillsyncbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        jobPostingRepository.deleteAll();

        userRepository.saveAll(List.of(
                new User("alice", "pass", "ROLE_USER", "alice@example.com"),
                new User("bob", "pass", "ROLE_RECRUITER", "bob@example.com"),
                new User("admin", "pass", "ROLE_ADMIN", "admin@example.com")
        ));

        jobPostingRepository.saveAll(List.of(
                new JobPosting("Dev", "Acme", "desc", List.of("Java"), "http://"),
                new JobPosting("QA", "Acme", "desc", List.of("Testing"), "http://")
        ));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testListUsersPaged() throws Exception {
        mockMvc.perform(get("/api/admin/users?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateUserRole() throws Exception {
        User u = userRepository.findByUsername("alice").orElseThrow();
        mockMvc.perform(put("/api/admin/users/" + u.getId() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("role", "ROLE_RECRUITER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_RECRUITER"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteUser() throws Exception {
        User u = userRepository.findByUsername("bob").orElseThrow();
        mockMvc.perform(delete("/api/admin/users/" + u.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testListJobsPaged() throws Exception {
        mockMvc.perform(get("/api/admin/jobs?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteJob() throws Exception {
        JobPosting j = jobPostingRepository.findAll().get(0);
        mockMvc.perform(delete("/api/admin/jobs/" + j.getId()))
                .andExpect(status().isOk());
    }
}
