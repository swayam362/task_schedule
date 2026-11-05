package com.studysync.repository;

import com.studysync.model.Exam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends MongoRepository<Exam, String> {
    List<Exam> findByUserId(String userId);
    List<Exam> findByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(String userId, LocalDate date);
}
