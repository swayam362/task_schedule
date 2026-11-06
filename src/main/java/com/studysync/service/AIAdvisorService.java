package com.studysync.service;

import com.studysync.model.*;
import com.studysync.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIAdvisorService {

    private final TaskRepository taskRepository;
    private final LearningPathRepository learningPathRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final RevisionRepository revisionRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final UserSettingsRepository settingsRepository;

    public AIAdvisorService(TaskRepository taskRepository,
                            LearningPathRepository learningPathRepository,
                            ExamRepository examRepository,
                            AssignmentRepository assignmentRepository,
                            RevisionRepository revisionRepository,
                            FocusSessionRepository focusSessionRepository,
                            UserSettingsRepository settingsRepository) {
        this.taskRepository = taskRepository;
        this.learningPathRepository = learningPathRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.revisionRepository = revisionRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.settingsRepository = settingsRepository;
    }

    public Map<String, Object> getWorkloadAnalysis(String userId) {
        LocalDate today = LocalDate.now();
        List<Task> allTasks = taskRepository.findByUserId(userId);
        List<LearningPath> paths = learningPathRepository.findByUserId(userId);
        List<Exam> exams = examRepository.findByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today);
        List<Assignment> assignments = assignmentRepository.findByUserIdAndSubmissionDeadlineGreaterThanEqualOrderBySubmissionDeadlineAsc(userId, today);
        List<Revision> revisions = revisionRepository.findByUserIdAndScheduledDateLessThanEqualAndStatus(userId, today, "Pending");
        UserSettings settings = settingsRepository.findByUserId(userId).orElse(new UserSettings(userId));

        // 1. Identify bottlenecks
        List<String> keyObservations = new ArrayList<>();
        List<String> actionItems = new ArrayList<>();
        List<String> topicRecommendations = new ArrayList<>();

        // Check missed tasks
        List<Task> missed = allTasks.stream().filter(t -> "Missed".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList());
        if (!missed.isEmpty()) {
            keyObservations.add("You have " + missed.size() + " missed or overdue task(s). Prioritize resolving high-priority backlogs first.");
            actionItems.add("Use the Missed Tasks view to reschedule '" + missed.get(0).getTitle() + "' to a realistic study window.");
        } else {
            keyObservations.add("Clean sheet: Zero overdue tasks! Your schedule discipline is in top form.");
        }

        // Check upcoming exams
        if (!exams.isEmpty()) {
            Exam nextExam = exams.get(0);
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, nextExam.getExamDate());
            if (daysLeft <= 7) {
                keyObservations.add("Upcoming Exam Alert: '" + nextExam.getName() + "' is in " + daysLeft + " day(s). Subject: " + nextExam.getSubject());
                actionItems.add("Dedicate at least 90 minutes daily to '" + nextExam.getSubject() + "' syllabus: " + (nextExam.getSyllabus() != null ? nextExam.getSyllabus() : "Core concepts"));
            }
        }

        // Check revisions
        if (!revisions.isEmpty()) {
            keyObservations.add("Spaced Repetition: " + revisions.size() + " topic revision(s) are due today to consolidate memory.");
            actionItems.add("Complete revision for '" + revisions.get(0).getTopicTitle() + "' before starting new complex theory.");
        }

        // Analyze learning paths progress
        for (LearningPath lp : paths) {
            if (lp.getProgressPercentage() < 50.0 && lp.getCurrentTopicTitle() != null) {
                topicRecommendations.add(lp.getTitle() + " &rarr; Resume at: " + lp.getCurrentTopicTitle());
            }
        }

        // Subject distribution balance
        Map<String, Long> categoryCounts = allTasks.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        long dsaCount = categoryCounts.getOrDefault("DSA", 0L);
        long backendCount = categoryCounts.getOrDefault("Java Backend", 0L);
        long dbmsCount = categoryCounts.getOrDefault("SQL and DBMS", 0L);
        long aiCount = categoryCounts.getOrDefault("AI/ML", 0L);

        String balanceVerdict;
        if (dsaCount > 0 && backendCount == 0 && dbmsCount == 0) {
            balanceVerdict = "Heavily skewed towards DSA practice. We recommend introducing Java Backend or DBMS topics to ensure balanced full-stack & core subject readiness.";
        } else if (backendCount > dsaCount * 2) {
            balanceVerdict = "Strong backend focus. Remember to solve at least 1-2 LeetCode problems daily to maintain algorithm fluency.";
        } else {
            balanceVerdict = "Healthy distribution across programming practice, backend development, and academic prep.";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("analysisDate", today.toString());
        response.put("balanceVerdict", balanceVerdict);
        response.put("keyObservations", keyObservations);
        response.put("actionItems", actionItems);
        response.put("topicRecommendations", topicRecommendations);
        response.put("categoryDistribution", categoryCounts);
        response.put("dailyStudyHoursTarget", settings.getDailyStudyHours());
        response.put("preferredStudyTime", settings.getPreferredStudyTime());

        return response;
    }

    public Map<String, Object> askAdvisor(String userId, String prompt) {
        String cleanPrompt = prompt != null ? prompt.toLowerCase().trim() : "";
        Map<String, Object> analysis = getWorkloadAnalysis(userId);

        String answer;
        String title;

        if (cleanPrompt.contains("exam") || cleanPrompt.contains("test") || cleanPrompt.contains("midterm")) {
            title = "Exam Preparation Strategy";
            answer = "Based on your active exam schedule, we recommend the 4-Day Micro-Revision Cycle:\n\n" +
                    "1. Day 1-2: Conceptual walkthrough of syllabus topics using active recall (test yourself before looking at notes).\n" +
                    "2. Day 3: Solve previous year question papers & practical lab questions under timed conditions.\n" +
                    "3. Day 4: Light formula & cheat-sheet review with short Pomodoro intervals (25m focus / 5m break).\n" +
                    "Remember to use the 'Generate Prep Tasks' button on your exam card to automatically block study slots.";
        } else if (cleanPrompt.contains("dsa") || cleanPrompt.contains("leetcode") || cleanPrompt.contains("algorithm")) {
            title = "DSA & LeetCode Progression Advice";
            answer = "For consistent DSA mastery:\n\n" +
                    "• Master one pattern at a time: Two Pointers &rarr; Sliding Window &rarr; Stacks/Queues &rarr; Binary Trees & Depth &rarr; Dynamic Programming.\n" +
                    "• Time yourself: Spend 25 minutes attempting a problem independently. If blocked, study the intuition, implement the solution in C++, and immediately schedule spaced repetition revisions to revisit it in 3 days.\n" +
                    "• Your current DSA focus is 'Binary Trees & Diameter'. Practice level-order traversal (BFS) and tree depth calculation.";
        } else if (cleanPrompt.contains("balance") || cleanPrompt.contains("routine") || cleanPrompt.contains("overwhelm")) {
            title = "Daily Study Balance & Fatigue Prevention";
            answer = (String) analysis.get("balanceVerdict") + "\n\n" +
                    "Tips for maximum cognitive stamina:\n" +
                    "• High-focus subjects (DSA problem solving) are best done in your preferred " + analysis.get("preferredStudyTime") + " slot.\n" +
                    "• Conceptual & project tasks (Spring Boot REST APIs, DBMS Lab submissions) fit well into afternoons and evenings.\n" +
                    "• Keep study blocks to 90 minutes maximum, separated by short 15-minute breaks.";
        } else {
            title = "Personalized StudySync Recommendation";
            answer = "Here is your current academic diagnostic:\n\n" +
                    "• " + String.join("\n• ", (List<String>) analysis.get("keyObservations")) + "\n\n" +
                    "Recommended Next Steps:\n" +
                    "• " + String.join("\n• ", (List<String>) analysis.get("actionItems"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("answer", answer);
        result.put("analysis", analysis);
        return result;
    }
}
