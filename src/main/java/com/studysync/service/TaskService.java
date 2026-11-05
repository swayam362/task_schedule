package com.studysync.service;

import com.studysync.dto.TaskConflictResponse;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.Task;
import com.studysync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskConflictResponse checkConflict(Task task) {
        if (task.getDate() == null || task.getStartTime() == null || task.getEndTime() == null) {
            return new TaskConflictResponse(false, null, null, null, null, null);
        }

        List<Task> existingTasks = taskRepository.findByUserIdAndDate(task.getUserId(), task.getDate());
        for (Task existing : existingTasks) {
            if (task.getId() != null && task.getId().equals(existing.getId())) {
                continue;
            }
            if ("Completed".equalsIgnoreCase(existing.getStatus())) {
                continue;
            }
            if (existing.getStartTime() != null && existing.getEndTime() != null) {
                boolean overlaps = task.getStartTime().isBefore(existing.getEndTime()) &&
                                   task.getEndTime().isAfter(existing.getStartTime());
                if (overlaps) {
                    return new TaskConflictResponse(
                        true,
                        "Schedule conflict detected with '" + existing.getTitle() + "' (" + existing.getStartTime() + " - " + existing.getEndTime() + ").",
                        existing.getId(),
                        existing.getTitle(),
                        existing.getStartTime(),
                        existing.getEndTime()
                    );
                }
            }
        }
        return new TaskConflictResponse(false, null, null, null, null, null);
    }

    public Task createTask(Task task, boolean forceOverride) {
        if (!forceOverride) {
            TaskConflictResponse conflict = checkConflict(task);
            if (conflict.isConflict()) {
                throw new IllegalStateException("CONFLICT:" + conflict.getMessage());
            }
        }

        if (task.getEstimatedDuration() == null && task.getStartTime() != null && task.getEndTime() != null) {
            int duration = (int) java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMinutes();
            if (duration > 0) {
                task.setEstimatedDuration(duration);
            }
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("Pending");
        }
        return taskRepository.save(task);
    }

    public Task updateTask(String id, Task updated, boolean forceOverride) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        updated.setId(id);
        updated.setUserId(task.getUserId());

        if (!forceOverride) {
            TaskConflictResponse conflict = checkConflict(updated);
            if (conflict.isConflict()) {
                throw new IllegalStateException("CONFLICT:" + conflict.getMessage());
            }
        }

        task.setTitle(updated.getTitle());
        task.setDescription(updated.getDescription());
        task.setCategory(updated.getCategory());
        task.setTaskType(updated.getTaskType());
        task.setDate(updated.getDate());
        task.setStartTime(updated.getStartTime());
        task.setEndTime(updated.getEndTime());
        task.setEstimatedDuration(updated.getEstimatedDuration());
        task.setPriority(updated.getPriority());
        task.setReminderMinutesBefore(updated.getReminderMinutesBefore());
        task.setNotes(updated.getNotes());
        
        if (updated.getStatus() != null && !updated.getStatus().equals(task.getStatus())) {
            task.setStatus(updated.getStatus());
            if ("Completed".equalsIgnoreCase(updated.getStatus())) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public Task markCompleted(String id) {
        Task task = getTaskById(id);
        task.setStatus("Completed");
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task changeStatus(String id, String status) {
        Task task = getTaskById(id);
        task.setStatus(status);
        if ("Completed".equalsIgnoreCase(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task rescheduleTask(String id, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime) {
        Task task = getTaskById(id);
        task.setDate(newDate);
        task.setStartTime(newStartTime);
        task.setEndTime(newEndTime);
        if ("Missed".equalsIgnoreCase(task.getStatus())) {
            task.setStatus("Pending");
        }
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<Task> getTasks(String userId, LocalDate date, String category, String priority, String status, String search) {
        List<Task> tasks;
        if (date != null) {
            tasks = taskRepository.findByUserIdAndDate(userId, date);
        } else {
            tasks = taskRepository.findByUserId(userId);
        }

        // Auto-check and flag missed tasks without destructive deletion
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        boolean changed = false;

        for (Task t : tasks) {
            if ("Pending".equalsIgnoreCase(t.getStatus()) || "In Progress".equalsIgnoreCase(t.getStatus())) {
                if (t.getDate() != null) {
                    if (t.getDate().isBefore(today)) {
                        t.setStatus("Missed");
                        changed = true;
                    } else if (t.getDate().isEqual(today) && t.getEndTime() != null && t.getEndTime().isBefore(now)) {
                        t.setStatus("Missed");
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            taskRepository.saveAll(tasks);
        }

        return tasks.stream()
                .filter(t -> category == null || category.isBlank() || t.getCategory().equalsIgnoreCase(category))
                .filter(t -> priority == null || priority.isBlank() || t.getPriority().equalsIgnoreCase(priority))
                .filter(t -> status == null || status.isBlank() || t.getStatus().equalsIgnoreCase(status))
                .filter(t -> search == null || search.isBlank() || 
                             (t.getTitle() != null && t.getTitle().toLowerCase().contains(search.toLowerCase())) ||
                             (t.getDescription() != null && t.getDescription().toLowerCase().contains(search.toLowerCase())))
                .sorted((a, b) -> {
                    // Sort by date, then startTime, then priority
                    if (a.getDate() != null && b.getDate() != null && !a.getDate().equals(b.getDate())) {
                        return a.getDate().compareTo(b.getDate());
                    }
                    if (a.getStartTime() != null && b.getStartTime() != null) {
                        return a.getStartTime().compareTo(b.getStartTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    public List<Task> getMissedTasks(String userId) {
        List<Task> all = taskRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Task> missed = new ArrayList<>();
        List<Task> toUpdate = new ArrayList<>();

        for (Task t : all) {
            if ("Completed".equalsIgnoreCase(t.getStatus())) {
                continue;
            }
            if ("Missed".equalsIgnoreCase(t.getStatus())) {
                missed.add(t);
                continue;
            }
            if (t.getDate() != null) {
                if (t.getDate().isBefore(today)) {
                    t.setStatus("Missed");
                    toUpdate.add(t);
                    missed.add(t);
                } else if (t.getDate().isEqual(today) && t.getEndTime() != null && t.getEndTime().isBefore(now)) {
                    t.setStatus("Missed");
                    toUpdate.add(t);
                    missed.add(t);
                }
            }
        }

        if (!toUpdate.isEmpty()) {
            taskRepository.saveAll(toUpdate);
        }

        missed.sort((a, b) -> {
            if (a.getDate() != null && b.getDate() != null) {
                return b.getDate().compareTo(a.getDate()); // most recently missed first
            }
            return 0;
        });
        return missed;
    }

    public List<Task> getTodayFocusTasks(String userId) {
        LocalDate today = LocalDate.now();
        List<Task> todayTasks = taskRepository.findByUserIdAndDate(userId, today);
        return todayTasks.stream()
                .filter(t -> !"Completed".equalsIgnoreCase(t.getStatus()))
                .sorted((a, b) -> {
                    // Priority rank
                    int prioA = "High".equalsIgnoreCase(a.getPriority()) ? 3 : ("Medium".equalsIgnoreCase(a.getPriority()) ? 2 : 1);
                    int prioB = "High".equalsIgnoreCase(b.getPriority()) ? 3 : ("Medium".equalsIgnoreCase(b.getPriority()) ? 2 : 1);
                    if (prioA != prioB) return Integer.compare(prioB, prioA);
                    if (a.getStartTime() != null && b.getStartTime() != null) {
                        return a.getStartTime().compareTo(b.getStartTime());
                    }
                    return 0;
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<Task> getTodayTimetable(String userId) {
        LocalDate today = LocalDate.now();
        List<Task> todayTasks = taskRepository.findByUserIdAndDate(userId, today);
        return todayTasks.stream()
                .sorted((a, b) -> {
                    if (a.getStartTime() != null && b.getStartTime() != null) {
                        return a.getStartTime().compareTo(b.getStartTime());
                    }
                    if (a.getStartTime() != null) return -1;
                    if (b.getStartTime() != null) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
    }
}
