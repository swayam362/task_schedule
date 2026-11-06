package com.studysync.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TaskConflictResponse {
    private boolean conflict;
    private String message;
    private String conflictingTaskId;
    private String conflictingTaskTitle;
    private LocalTime conflictingStartTime;
    private LocalTime conflictingEndTime;

    public TaskConflictResponse() {}

    public TaskConflictResponse(boolean conflict, String message, String conflictingTaskId, String conflictingTaskTitle, LocalTime conflictingStartTime, LocalTime conflictingEndTime) {
        this.conflict = conflict;
        this.message = message;
        this.conflictingTaskId = conflictingTaskId;
        this.conflictingTaskTitle = conflictingTaskTitle;
        this.conflictingStartTime = conflictingStartTime;
        this.conflictingEndTime = conflictingEndTime;
    }

    public boolean isConflict() { return conflict; }
    public void setConflict(boolean conflict) { this.conflict = conflict; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getConflictingTaskId() { return conflictingTaskId; }
    public void setConflictingTaskId(String conflictingTaskId) { this.conflictingTaskId = conflictingTaskId; }

    public String getConflictingTaskTitle() { return conflictingTaskTitle; }
    public void setConflictingTaskTitle(String conflictingTaskTitle) { this.conflictingTaskTitle = conflictingTaskTitle; }

    public LocalTime getConflictingStartTime() { return conflictingStartTime; }
    public void setConflictingStartTime(LocalTime conflictingStartTime) { this.conflictingStartTime = conflictingStartTime; }

    public LocalTime getConflictingEndTime() { return conflictingEndTime; }
    public void setConflictingEndTime(LocalTime conflictingEndTime) { this.conflictingEndTime = conflictingEndTime; }
}

