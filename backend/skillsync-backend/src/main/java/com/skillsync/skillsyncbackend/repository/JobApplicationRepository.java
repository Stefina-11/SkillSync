package com.skillsync.skillsyncbackend.repository;

import com.skillsync.skillsyncbackend.model.JobApplication;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUser(User user);
    List<JobApplication> findByJobPosting(JobPosting jobPosting);
}