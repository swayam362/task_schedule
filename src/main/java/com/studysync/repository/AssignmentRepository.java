package com.studysync.repository;

import com.studysync.model.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssignmentRepository extends MongoRepository<Assignment, String> {
    List<Assignment> findByUserId(String userId);
    List<Assignment> findByUserIdAndSubmissionDeadlineGreaterThanEqualOrderBySubmissionDeadlineAsc(String userId, LocalDate date);
    List<Assignment> findByUserIdAndStatus(String userId, String status);
}

