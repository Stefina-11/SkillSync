package com.skillsync.skillsyncbackend.service;

import com.skillsync.skillsyncbackend.model.JobPosting;
import com.skillsync.skillsyncbackend.repository.JobPostingRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JobScrapingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public List<JobPosting> fetchAndSaveJobPostings() {
        List<JobPosting> jobPostings = new ArrayList<>();
        // Mock data for now
        jobPostings.add(new JobPosting("Software Engineer", "Google", "Develop and maintain software.", Arrays.asList("Java", "Spring Boot", "REST API", "SQL"), "http://mockjobs.com/google-se"));
        jobPostings.add(new JobPosting("Frontend Developer", "Facebook", "Build user interfaces.", Arrays.asList("JavaScript", "React", "HTML", "CSS"), "http://mockjobs.com/facebook-fe"));
        jobPostings.add(new JobPosting("Data Scientist", "Amazon", "Analyze large datasets.", Arrays.asList("Python", "Machine Learning", "SQL", "Statistics"), "http://mockjobs.com/amazon-ds"));
        jobPostings.add(new JobPosting("Backend Developer", "Microsoft", "Design and implement backend services.", Arrays.asList("Java", "Spring Boot", "Microservices", "Kafka"), "http://mockjobs.com/microsoft-be"));
        jobPostings.add(new JobPosting("DevOps Engineer", "Netflix", "Manage infrastructure and deployments.", Arrays.asList("AWS", "Docker", "Kubernetes", "CI/CD"), "http://mockjobs.com/netflix-devops"));

        jobPostingRepository.saveAll(jobPostings);
        return jobPostings;
    }

    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    private List<String> extractSkills(String text) {
        return Arrays.asList("Java", "Spring Boot", "REST API", "SQL", "Maven");
    }
}
