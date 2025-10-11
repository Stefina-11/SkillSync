package com.skillsync.skillsyncbackend;

import com.skillsync.skillsyncbackend.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
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
    }
}
