package com.studysync.service;

import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.Revision;
import com.studysync.model.Task;
import com.studysync.repository.RevisionRepository;
import com.studysync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RevisionService {

    private final RevisionRepository revisionRepository;
    private final TaskRepository taskRepository;

    public RevisionService(RevisionRepository revisionRepository, TaskRepository taskRepository) {
        this.revisionRepository = revisionRepository;
        this.taskRepository = taskRepository;
    }

    public List<Revision> getRevisions(String userId, String filter) {
        LocalDate today = LocalDate.now();
        List<Revision> all = revisionRepository.findByUserId(userId);

        // Auto-check for missed revisions
        for (Revision r : all) {
            if ("Pending".equalsIgnoreCase(r.getStatus()) && r.getScheduledDate().isBefore(today)) {
                r.setStatus("Missed");
                revisionRepository.save(r);
            }
        }

        if ("today".equalsIgnoreCase(filter)) {
            return revisionRepository.findByUserIdAndScheduledDate(userId, today);
        } else if ("upcoming".equalsIgnoreCase(filter)) {
            return revisionRepository.findByUserIdAndScheduledDateBetween(userId, today.plusDays(1), today.plusDays(30));
        } else if ("missed".equalsIgnoreCase(filter)) {
            return revisionRepository.findByUserIdAndStatus(userId, "Missed");
        } else if ("completed".equalsIgnoreCase(filter)) {
            return revisionRepository.findByUserIdAndStatus(userId, "Completed");
        }
        return all;
    }

    public List<Revision> scheduleRevisionsForTopic(String userId, String pathId, String pathTitle, String topicId, String topicTitle, List<Integer> intervals, boolean createTasks) {
        if (intervals == null || intervals.isEmpty()) {
            intervals = List.of(1, 3, 7, 15);
        }

        LocalDate today = LocalDate.now();
        List<Revision> createdRevisions = new ArrayList<>();

        for (int i = 0; i < intervals.size(); i++) {
            int days = intervals.get(i);
            LocalDate scheduledDate = today.plusDays(days);
            int revisionNumber = i + 1;

            Revision revision = new Revision(userId, pathId, pathTitle, topicId, topicTitle, revisionNumber, scheduledDate);
            revision.setNotes("Spaced Repetition #" + revisionNumber + " (+" + days + "d)");
            Revision saved = revisionRepository.save(revision);
            createdRevisions.add(saved);

            if (createTasks) {
                Task task = new Task();
                task.setUserId(userId);
                task.setTitle("Revise: " + topicTitle + " (Rev #" + revisionNumber + ")");
                task.setDescription("Spaced repetition revision for " + pathTitle + " - Topic: " + topicTitle);
                task.setCategory(pathTitle != null && pathTitle.contains("DSA") ? "DSA" : 
                                 (pathTitle != null && pathTitle.contains("Java") ? "Java Backend" : 
                                 (pathTitle != null && pathTitle.contains("Python") ? "Python" : 
                                 (pathTitle != null && pathTitle.contains("SQL") ? "SQL and DBMS" : "Other"))));
                task.setTaskType("Revision");
                task.setDate(scheduledDate);
                task.setStartTime(LocalTime.of(9, 0));
                task.setEndTime(LocalTime.of(9, 45));
                task.setEstimatedDuration(45);
                task.setPriority("Medium");
                task.setReminderMinutesBefore(10);
                task.setLinkedEntityId(saved.getId());
                task.setLinkedEntityType("Revision");
                task.setStatus("Pending");
                taskRepository.save(task);
            }
        }

        return createdRevisions;
    }

    public Revision createRevision(Revision revision) {
        if (revision.getScheduledDate() == null) {
            revision.setScheduledDate(LocalDate.now());
        }
        revision.setStatus("Pending");
        revision.setCreatedAt(LocalDateTime.now());
        return revisionRepository.save(revision);
    }

    public Revision markCompleted(String id) {
        Revision revision = revisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found with id: " + id));
        revision.setStatus("Completed");
        revision.setCompletedAt(LocalDateTime.now());
        return revisionRepository.save(revision);
    }

    public Revision reschedule(String id, LocalDate newDate) {
        Revision revision = revisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found with id: " + id));
        revision.setScheduledDate(newDate);
        revision.setStatus("Pending");
        return revisionRepository.save(revision);
    }

    public void deleteRevision(String id) {
        if (!revisionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Revision not found with id: " + id);
        }
        revisionRepository.deleteById(id);
    }
}
