package com.studysync.service;

import com.studysync.model.Assignment;
import com.studysync.model.Exam;
import com.studysync.model.Revision;
import com.studysync.model.Task;
import com.studysync.repository.AssignmentRepository;
import com.studysync.repository.ExamRepository;
import com.studysync.repository.RevisionRepository;
import com.studysync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CalendarSyncService {

    private final TaskRepository taskRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final RevisionRepository revisionRepository;

    private static final DateTimeFormatter ICS_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public CalendarSyncService(TaskRepository taskRepository,
                               ExamRepository examRepository,
                               AssignmentRepository assignmentRepository,
                               RevisionRepository revisionRepository) {
        this.taskRepository = taskRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.revisionRepository = revisionRepository;
    }

    public String generateICalendar(String userId) {
        StringBuilder sb = new StringBuilder();
        String nowStr = LocalDateTime.now().format(ICS_DATE_TIME);

        // Header
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//StudySync//Smart Study & Task Scheduler//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:StudySync Schedule & Exams\r\n");
        sb.append("X-WR-TIMEZONE:Asia/Kolkata\r\n");

        // 1. Tasks
        List<Task> tasks = taskRepository.findByUserId(userId);
        for (Task t : tasks) {
            if (t.getDate() == null) continue;

            LocalTime start = t.getStartTime() != null ? t.getStartTime() : LocalTime.of(9, 0);
            LocalTime end = t.getEndTime() != null ? t.getEndTime() : start.plusMinutes(t.getEstimatedDuration() != null ? t.getEstimatedDuration() : 60);

            LocalDateTime startDt = t.getDate().atTime(start);
            LocalDateTime endDt = t.getDate().atTime(end);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:studysync-task-").append(t.getId()).append("@studysync.app\r\n");
            sb.append("DTSTAMP:").append(nowStr).append("\r\n");
            sb.append("DTSTART:").append(startDt.format(ICS_DATE_TIME)).append("\r\n");
            sb.append("DTEND:").append(endDt.format(ICS_DATE_TIME)).append("\r\n");
            sb.append("SUMMARY:[").append(escapeIcs(t.getCategory() != null ? t.getCategory() : "Study")).append("] ")
              .append(escapeIcs(t.getTitle())).append("\r\n");

            String desc = (t.getDescription() != null ? t.getDescription() : "") +
                          (t.getNotes() != null ? "\\nNotes: " + t.getNotes() : "") +
                          "\\nPriority: " + (t.getPriority() != null ? t.getPriority() : "Medium") +
                          "\\nManaged via StudySync";
            sb.append("DESCRIPTION:").append(escapeIcs(desc)).append("\r\n");
            sb.append("STATUS:").append("Completed".equalsIgnoreCase(t.getStatus()) ? "COMPLETED" : "CONFIRMED").append("\r\n");

            // Alarm for phone notification (10m before or user custom reminder)
            int reminderMins = t.getReminderMinutesBefore() != null ? t.getReminderMinutesBefore() : 10;
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(reminderMins).append("M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder: ").append(escapeIcs(t.getTitle())).append(" starts in ").append(reminderMins).append(" mins!\r\n");
            sb.append("END:VALARM\r\n");

            sb.append("END:VEVENT\r\n");
        }

        // 2. Exams
        List<Exam> exams = examRepository.findByUserId(userId);
        for (Exam e : exams) {
            if (e.getExamDate() == null) continue;

            LocalTime examTime = e.getExamTime() != null ? e.getExamTime() : LocalTime.of(10, 0);
            LocalDateTime examDt = e.getExamDate().atTime(examTime);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:studysync-exam-").append(e.getId()).append("@studysync.app\r\n");
            sb.append("DTSTAMP:").append(nowStr).append("\r\n");
            sb.append("DTSTART:").append(examDt.format(ICS_DATE_TIME)).append("\r\n");
            sb.append("DTEND:").append(examDt.plusHours(3).format(ICS_DATE_TIME)).append("\r\n");
            sb.append("SUMMARY:🚨 EXAM: ").append(escapeIcs(e.getName())).append(" (").append(escapeIcs(e.getSubject())).append(")\r\n");

            String examDesc = "Subject: " + e.getSubject() +
                              "\\nSyllabus: " + (e.getSyllabus() != null ? e.getSyllabus() : "Complete Course") +
                              "\\nTarget Score: " + (e.getTargetScore() != null ? e.getTargetScore() : "Best") +
                              (e.getNotes() != null ? "\\nNotes: " + e.getNotes() : "");
            sb.append("DESCRIPTION:").append(escapeIcs(examDesc)).append("\r\n");

            // Exam Alarms: 1 day before AND 1 hour before
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-P1D\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Exam Tomorrow: ").append(escapeIcs(e.getName())).append("\r\n");
            sb.append("END:VALARM\r\n");

            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT1H\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Exam in 1 Hour: ").append(escapeIcs(e.getName())).append("\r\n");
            sb.append("END:VALARM\r\n");

            sb.append("END:VEVENT\r\n");
        }

        // 3. Assignments & Practicals
        List<Assignment> assignments = assignmentRepository.findByUserId(userId);
        for (Assignment a : assignments) {
            if (a.getSubmissionDeadline() == null) continue;

            LocalTime subTime = a.getSubmissionTime() != null ? a.getSubmissionTime() : LocalTime.of(23, 59);
            LocalDateTime deadlineDt = a.getSubmissionDeadline().atTime(subTime);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:studysync-assign-").append(a.getId()).append("@studysync.app\r\n");
            sb.append("DTSTAMP:").append(nowStr).append("\r\n");
            sb.append("DTSTART:").append(deadlineDt.minusHours(1).format(ICS_DATE_TIME)).append("\r\n");
            sb.append("DTEND:").append(deadlineDt.format(ICS_DATE_TIME)).append("\r\n");
            sb.append("SUMMARY:📋 DEADLINE: ").append(escapeIcs(a.getTitle())).append("\r\n");

            String aDesc = "Subject: " + (a.getSubject() != null ? a.getSubject() : "") +
                           "\\nType: " + (a.getType() != null ? a.getType() : "Assignment") +
                           (a.getPracticalDetails() != null ? "\\nLab Details: " + a.getPracticalDetails() : "") +
                           (a.getDetails() != null ? "\\nGuidelines: " + a.getDetails() : "");
            sb.append("DESCRIPTION:").append(escapeIcs(aDesc)).append("\r\n");

            // Alarm 2 hours before deadline
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT2H\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Assignment Deadline in 2 Hours: ").append(escapeIcs(a.getTitle())).append("\r\n");
            sb.append("END:VALARM\r\n");

            sb.append("END:VEVENT\r\n");
        }

        // 4. Revisions
        List<Revision> revisions = revisionRepository.findByUserId(userId);
        for (Revision r : revisions) {
            if (r.getScheduledDate() == null || "Completed".equalsIgnoreCase(r.getStatus())) continue;

            LocalDateTime revStart = r.getScheduledDate().atTime(LocalTime.of(8, 30));
            LocalDateTime revEnd = revStart.plusMinutes(45);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:studysync-rev-").append(r.getId()).append("@studysync.app\r\n");
            sb.append("DTSTAMP:").append(nowStr).append("\r\n");
            sb.append("DTSTART:").append(revStart.format(ICS_DATE_TIME)).append("\r\n");
            sb.append("DTEND:").append(revEnd.format(ICS_DATE_TIME)).append("\r\n");
            sb.append("SUMMARY:🔄 Spaced Revision: ").append(escapeIcs(r.getTopicTitle())).append(" (Rev #").append(r.getRevisionNumber()).append(")\r\n");
            sb.append("DESCRIPTION:Course: ").append(escapeIcs(r.getLearningPathTitle() != null ? r.getLearningPathTitle() : "")).append("\r\n");

            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT15M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Scheduled Revision: ").append(escapeIcs(r.getTopicTitle())).append(" starts in 15 mins\r\n");
            sb.append("END:VALARM\r\n");

            sb.append("END:VEVENT\r\n");
        }

        // Footer
        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
}

