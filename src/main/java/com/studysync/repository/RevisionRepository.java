package com.studysync.repository;

import com.studysync.model.Revision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevisionRepository extends MongoRepository<Revision, String> {
    List<Revision> findByUserId(String userId);
    List<Revision> findByUserIdAndScheduledDate(String userId, LocalDate date);
    List<Revision> findByUserIdAndScheduledDateLessThanEqualAndStatus(String userId, LocalDate date, String status);
    List<Revision> findByUserIdAndScheduledDateBetween(String userId, LocalDate start, LocalDate end);
    List<Revision> findByUserIdAndStatus(String userId, String status);
    List<Revision> findByUserIdAndTopicId(String userId, String topicId);
}
