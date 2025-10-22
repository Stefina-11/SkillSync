package com.skillsync.skillsyncbackend;

import com.skillsync.skillsyncbackend.model.JobPosting;
import com.skillsync.skillsyncbackend.repository.JobPostingRepository;
import com.skillsync.skillsyncbackend.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final JobPostingRepository jobPostingRepository;

    public DataInitializer(UserService userService, JobPostingRepository jobPostingRepository) {
        this.userService = userService;
        this.jobPostingRepository = jobPostingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userService.findByUsername("admin").isEmpty() && userService.findByEmail("admin@example.com").isEmpty()) {
            userService.registerUser("admin", "adminpass", "ADMIN", "admin@example.com");
            System.out.println("Default admin user created: admin/adminpass");
        }
        if (userService.findByUsername("user").isEmpty() && userService.findByEmail("user@example.com").isEmpty()) {
            userService.registerUser("user", "userpass", "USER", "user@example.com");
            System.out.println("Default user created: user/userpass");
        }
        if (userService.findByUsername("recruiter").isEmpty() && userService.findByEmail("recruiter@example.com").isEmpty()) {
            userService.registerUser("recruiter", "recruiterpass", "RECRUITER", "recruiter@example.com");
            System.out.println("Default recruiter user created: recruiter/recruiterpass");
        }
        if (jobPostingRepository.count() == 0) {
            // Assuming 'recruiter' user gets ID 3 based on the order of creation in this initializer
            // In a real application, you would fetch the recruiter's ID dynamically.
            Long recruiterId = userService.findByUsername("recruiter").map(user -> user.getId()).orElse(3L);

            List<JobPosting> initialJobPostings = Arrays.asList(
                    new JobPosting("Software Engineer", "Tech Solutions Inc.", "Develop and maintain software applications.", Arrays.asList("Java", "Spring Boot", "Microservices"), "http://techsolutions.com/se", "New York", "Full-time", 100000.00, recruiterId),
                    new JobPosting("Frontend Developer", "Web Innovations", "Build responsive user interfaces.", Arrays.asList("JavaScript", "React", "HTML", "CSS"), "http://webinnovations.com/fd", "Remote", "Full-time", 90000.00, recruiterId),
                    new JobPosting("Data Scientist", "Data Insights Corp.", "Analyze complex data sets and build predictive models.", Arrays.asList("Python", "R", "Machine Learning", "SQL"), "http://datainsights.com/ds", "San Francisco", "Full-time", 120000.00, recruiterId),
                    new JobPosting("UX Designer", "Creative Minds", "Design intuitive and engaging user experiences.", Arrays.asList("Figma", "Sketch", "User Research"), "http://creativeminds.com/ux", "London", "Contract", 75000.00, recruiterId),
                    new JobPosting("DevOps Engineer", "Cloud Builders", "Manage and optimize cloud infrastructure.", Arrays.asList("AWS", "Docker", "Kubernetes", "CI/CD"), "http://cloudbuilders.com/devops", "Seattle", "Full-time", 110000.00, recruiterId),
                    new JobPosting("Full Stack Developer", "Global Tech Solutions", "Develop and maintain both frontend and backend systems.", Arrays.asList("Java", "Spring Boot", "Angular", "TypeScript", "SQL"), "http://globaltech.com/fsd", "Chennai", "Full-time", 130000.00, recruiterId)
            );
            jobPostingRepository.saveAll(initialJobPostings);
            System.out.println("Initialized " + initialJobPostings.size() + " job postings.");
        }
    }
}
