package com.skillsync.skillsyncbackend.repository;

import com.skillsync.skillsyncbackend.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    @Query("SELECT j FROM JobPosting j WHERE " +
           "(:keyword IS NULL OR lower(j.title) LIKE lower(concat('%', :keyword, '%')) OR " +
           "lower(j.company) LIKE lower(concat('%', :keyword, '%')) OR " +
           "lower(j.description) LIKE lower(concat('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR lower(j.location) LIKE lower(concat('%', :location, '%'))) AND " +
           "(:jobType IS NULL OR lower(j.jobType) LIKE lower(concat('%', :jobType, '%'))) AND " +
           "(:minSalary IS NULL OR j.salary >= :minSalary)")
    List<JobPosting> findByFilters(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jobType") String jobType,
            @Param("minSalary") Double minSalary
    );
}
