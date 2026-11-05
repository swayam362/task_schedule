package com.studysync.repository;

import com.studysync.model.LearningPath;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends MongoRepository<LearningPath, String> {
    List<LearningPath> findByUserId(String userId);
    List<LearningPath> findByUserIdAndCategory(String userId, String category);
}
