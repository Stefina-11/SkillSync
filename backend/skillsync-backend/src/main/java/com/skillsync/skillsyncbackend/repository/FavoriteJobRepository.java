package com.skillsync.skillsyncbackend.repository;

import com.skillsync.skillsyncbackend.model.FavoriteJob;
import com.skillsync.skillsyncbackend.model.User;
import com.skillsync.skillsyncbackend.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteJobRepository extends JpaRepository<FavoriteJob, Long> {
    List<FavoriteJob> findByUser(User user);
    Optional<FavoriteJob> findByUserAndJobPosting(User user, JobPosting jobPosting);
}
