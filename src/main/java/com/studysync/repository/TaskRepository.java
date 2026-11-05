package com.studysync.repository;

import com.studysync.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserId(String userId);
    List<Task> findByUserIdAndDate(String userId, LocalDate date);
    List<Task> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
    List<Task> findByUserIdAndStatus(String userId, String status);
    List<Task> findByUserIdAndCategory(String userId, String category);
    List<Task> findByUserIdAndPriority(String userId, String priority);
    List<Task> findByUserIdAndDateLessThanEqualAndStatusIn(String userId, LocalDate date, Collection<String> statuses);
}
