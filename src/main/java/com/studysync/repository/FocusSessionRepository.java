package com.studysync.repository;

import com.studysync.model.FocusSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FocusSessionRepository extends MongoRepository<FocusSession, String> {
    List<FocusSession> findByUserId(String userId);
    List<FocusSession> findByUserIdAndCompletedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
}

