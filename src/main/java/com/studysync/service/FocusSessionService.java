package com.studysync.service;

import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.FocusSession;
import com.studysync.model.Task;
import com.studysync.repository.FocusSessionRepository;
import com.studysync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FocusSessionService {

    private final FocusSessionRepository sessionRepository;
    private final TaskRepository taskRepository;

    public FocusSessionService(FocusSessionRepository sessionRepository, TaskRepository taskRepository) {
        this.sessionRepository = sessionRepository;
        this.taskRepository = taskRepository;
    }

    public FocusSession recordSession(FocusSession session) {
        session.setCompletedAt(LocalDateTime.now());
        FocusSession saved = sessionRepository.save(session);

        if (session.getTaskId() != null && !session.getTaskId().isBlank()) {
            taskRepository.findById(session.getTaskId()).ifPresent(task -> {
                if ("Pending".equalsIgnoreCase(task.getStatus())) {
                    task.setStatus("In Progress");
                }
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task);
            });
        }

        return saved;
    }

    public List<FocusSession> getSessions(String userId, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return sessionRepository.findByUserIdAndCompletedAtBetween(userId, start, end);
        }
        return sessionRepository.findByUserId(userId);
    }
}

