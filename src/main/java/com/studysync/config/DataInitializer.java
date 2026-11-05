package com.studysync.config;

import com.studysync.model.*;
import com.studysync.repository.*;
import com.studysync.service.LearningService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final LearningPathRepository learningPathRepository;
    private final TaskRepository taskRepository;
    private final RevisionRepository revisionRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final LearningService learningService;

    public DataInitializer(UserRepository userRepository,
                           UserSettingsRepository settingsRepository,
                           LearningPathRepository learningPathRepository,
                           TaskRepository taskRepository,
                           RevisionRepository revisionRepository,
                           ExamRepository examRepository,
                           AssignmentRepository assignmentRepository,
                           FocusSessionRepository focusSessionRepository,
                           LearningService learningService) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.learningPathRepository = learningPathRepository;
        this.taskRepository = taskRepository;
        this.revisionRepository = revisionRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.learningService = learningService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println(">>> Initializing StudySync Default Database Records...");

        // 1. Create Default User
        User user = new User("user-sainath", "sainath", "sainath@student.cse.edu", "Sainath", "B.Tech Computer Science & Engineering");
        userRepository.save(user);

        // 2. Create User Settings
        UserSettings settings = new UserSettings("user-sainath");
        settings.setDailyStudyHours(6);
        settings.setPreferredStudyTime("Morning");
        settings.setRevisionIntervals(List.of(1, 3, 7, 15));
        settings.setNotificationSound(true);
        settings.setBrowserNotificationsEnabled(true);
        settings.setReminderMinutesBefore(10);
        settings.setThemeMode("light");
        settings.setWeekStartDay("Monday");
        settings.setFocusDuration(25);
        settings.setShortBreakDuration(5);
        settings.setLongBreakDuration(15);
        settingsRepository.save(settings);

        // 3. Create Learning Paths
        // Path 1: DSA with C++
        LearningPath dsaPath = new LearningPath("user-sainath", "DSA with C++ and LeetCode Practice", "Master core data structures, algorithms, problem-solving and LeetCode patterns.", "DSA", "code", "emerald");
        Subject dsaSub1 = new Subject(UUID.randomUUID().toString(), "Data Structures Core", "Fundamental linear and hierarchical structures");
        Chapter dsaCh1 = new Chapter(UUID.randomUUID().toString(), "Arrays & Strings", "Array manipulations, two pointers, sliding window");
        Topic t1 = new Topic(UUID.randomUUID().toString(), "Arrays Basics & Two Pointers", "Two pointer patterns, in-place rotation, Dutch national flag.");
        t1.setStatus("Completed");
        t1.setStudyMinutes(90);
        t1.setNotes("Two-pointer technique is essential for O(N) linear time on sorted arrays. Practiced LeetCode #11, #15, #75.");
        Topic t2 = new Topic(UUID.randomUUID().toString(), "Sliding Window & Prefix Sum", "Fixed and dynamic sliding window patterns.");
        t2.setStatus("Completed");
        t2.setStudyMinutes(80);
        t2.setNotes("Kadane's algorithm for max subarray sum. Subarray sum equals K with HashMaps.");
        dsaCh1.getTopics().addAll(List.of(t1, t2));

        Chapter dsaCh2 = new Chapter(UUID.randomUUID().toString(), "Trees & Binary Search Trees", "Binary trees, traversals, BST properties");
        Topic t3 = new Topic(UUID.randomUUID().toString(), "Tree Traversals & Diameter of Binary Tree", "Preorder, Inorder, Postorder recursive and iterative; Tree height & diameter.");
        t3.setStatus("In Progress");
        t3.setStudyMinutes(45);
        t3.setNotes("Iterative traversals using explicit Stack. Diameter formula: max(left_height + right_height).");
        Topic t4 = new Topic(UUID.randomUUID().toString(), "Lowest Common Ancestor & BST Validation", "LCA in BT and BST; checking valid BST with min/max bounds.");
        t4.setStatus("Pending");
        dsaCh2.getTopics().addAll(List.of(t3, t4));
        dsaSub1.getChapters().addAll(List.of(dsaCh1, dsaCh2));

        Subject dsaSub2 = new Subject(UUID.randomUUID().toString(), "Advanced Algorithms", "Dynamic programming, graphs and greedy");
        Chapter dsaCh3 = new Chapter(UUID.randomUUID().toString(), "Dynamic Programming", "Memoization, tabulation and optimization");
        Topic t5 = new Topic(UUID.randomUUID().toString(), "0/1 Knapsack & Subset Sum", "Core classical DP pattern with bounded items.");
        t5.setStatus("Pending");
        dsaCh3.getTopics().add(t5);
        dsaSub2.getChapters().add(dsaCh3);

        dsaPath.getSubjects().addAll(List.of(dsaSub1, dsaSub2));
        dsaPath.setLastStudiedTopicId(t2.getId());
        dsaPath.setLastStudiedTopicTitle(t2.getTitle());
        dsaPath.setCurrentTopicId(t3.getId());
        dsaPath.setCurrentTopicTitle(t3.getTitle());
        dsaPath.setNextTopicId(t4.getId());
        dsaPath.setNextTopicTitle(t4.getTitle());
        dsaPath.setLastStudiedAt(LocalDateTime.now().minusHours(4));
        dsaPath.setNotes("Currently mastering Tree Traversals and Diameter problem on LeetCode.");
        learningService.recalculateStats(dsaPath);
        learningPathRepository.save(dsaPath);

        // Path 2: Java Backend
        LearningPath javaPath = new LearningPath("user-sainath", "Java Backend Development", "Enterprise backend development with modern Java, Spring Boot and microservices.", "Java Backend", "server", "blue");
        Subject javaSub1 = new Subject(UUID.randomUUID().toString(), "Core Java & Concurrency", "Deep language internals, OOP and JVM");
        Chapter javaCh1 = new Chapter(UUID.randomUUID().toString(), "OOP & Collections", "Object orientation, collections framework internals");
        Topic jt1 = new Topic(UUID.randomUUID().toString(), "Core Java OOP Principles", "Encapsulation, Polymorphism, Abstraction, Interfaces");
        jt1.setStatus("Completed");
        jt1.setStudyMinutes(120);
        Topic jt2 = new Topic(UUID.randomUUID().toString(), "Collections Framework & Generics", "HashMap internals, bucketing, treeify threshold, ArrayList vs LinkedList.");
        jt2.setStatus("Completed");
        jt2.setStudyMinutes(100);
        Topic jt3 = new Topic(UUID.randomUUID().toString(), "Multithreading & Concurrency", "ExecutorService, ThreadPools, Synchronized blocks, volatile.");
        jt3.setStatus("Completed");
        jt3.setStudyMinutes(90);
        javaCh1.getTopics().addAll(List.of(jt1, jt2, jt3));

        Chapter javaCh2 = new Chapter(UUID.randomUUID().toString(), "Persistence & Database Connectivity", "JDBC, Transactions, Connection Pooling");
        Topic jt4 = new Topic(UUID.randomUUID().toString(), "JDBC & PreparedStatement", "Prepared statement caching, SQL injection protection, connection pooling with HikariCP.");
        jt4.setStatus("Completed");
        jt4.setStudyMinutes(85);
        jt4.setNotes("Mastered batch statements and HikariCP connection pool configurations.");
        javaCh2.getTopics().add(jt4);
        javaSub1.getChapters().addAll(List.of(javaCh1, javaCh2));

        Subject javaSub2 = new Subject(UUID.randomUUID().toString(), "Spring Framework Ecosystem", "Spring Core, Spring Boot, Data and Security");
        Chapter javaCh3 = new Chapter(UUID.randomUUID().toString(), "Spring Core & Web", "Inversion of Control, Dependency Injection, REST APIs");
        Topic jt5 = new Topic(UUID.randomUUID().toString(), "Spring Framework Core & Dependency Injection", "ApplicationContext, Bean lifecycle, @Component, @Service, @Autowired.");
        jt5.setStatus("In Progress");
        jt5.setStudyMinutes(60);
        jt5.setNotes("Understanding Bean factory post-processors and constructor injection best practices.");
        Topic jt6 = new Topic(UUID.randomUUID().toString(), "Spring Boot & REST API Development", "Auto-configuration, Spring Web MVC, Controllers, Request mapping and DTO validation.");
        jt6.setStatus("Pending");
        javaCh3.getTopics().addAll(List.of(jt5, jt6));
        javaSub2.getChapters().add(javaCh3);

        javaPath.getSubjects().addAll(List.of(javaSub1, javaSub2));
        javaPath.setLastStudiedTopicId(jt4.getId());
        javaPath.setLastStudiedTopicTitle(jt4.getTitle());
        javaPath.setCurrentTopicId(jt5.getId());
        javaPath.setCurrentTopicTitle(jt5.getTitle());
        javaPath.setNextTopicId(jt6.getId());
        javaPath.setNextTopicTitle(jt6.getTitle());
        javaPath.setLastStudiedAt(LocalDateTime.now().minusHours(2));
        javaPath.setNotes("Completed JDBC & HikariCP; diving into Spring Boot dependency injection and bean lifecycles.");
        learningService.recalculateStats(javaPath);
        learningPathRepository.save(javaPath);

        // Path 3: Python Programming and AI/ML
        LearningPath pyPath = new LearningPath("user-sainath", "Python and AI/ML Learning", "From Python syntax to machine learning, model training and evaluation.", "AI/ML", "cpu", "purple");
        Subject pySub1 = new Subject(UUID.randomUUID().toString(), "Python Scientific Computing", "NumPy, Pandas and Data Processing");
        Chapter pyCh1 = new Chapter(UUID.randomUUID().toString(), "Data Wrangling", "Vectorization, array broadcasting, Series & DataFrames");
        Topic pt1 = new Topic(UUID.randomUUID().toString(), "NumPy Vectorized Computing", "Array creation, slicing, broadcasting rules, matrix math.");
        pt1.setStatus("Completed");
        pt1.setStudyMinutes(70);
        Topic pt2 = new Topic(UUID.randomUUID().toString(), "Pandas Data Cleaning & Aggregations", "Missing values, groupby, pivot tables, merge/joins.");
        pt2.setStatus("In Progress");
        pt2.setStudyMinutes(50);
        Topic pt3 = new Topic(UUID.randomUUID().toString(), "Linear & Logistic Regression", "Loss functions, gradient descent, train/test split, Scikit-learn.");
        pt3.setStatus("Pending");
        pyCh1.getTopics().addAll(List.of(pt1, pt2, pt3));
        pySub1.getChapters().add(pyCh1);

        pyPath.getSubjects().add(pySub1);
        pyPath.setLastStudiedTopicId(pt1.getId());
        pyPath.setLastStudiedTopicTitle(pt1.getTitle());
        pyPath.setCurrentTopicId(pt2.getId());
        pyPath.setCurrentTopicTitle(pt2.getTitle());
        pyPath.setNextTopicId(pt3.getId());
        pyPath.setNextTopicTitle(pt3.getTitle());
        pyPath.setLastStudiedAt(LocalDateTime.now().minusDays(1));
        learningService.recalculateStats(pyPath);
        learningPathRepository.save(pyPath);

        // Path 4: SQL & DBMS Preparation
        LearningPath dbPath = new LearningPath("user-sainath", "SQL and DBMS Preparation", "Relational database concepts, normalization, query optimization and ACID transactions.", "SQL and DBMS", "database", "amber");
        Subject dbSub1 = new Subject(UUID.randomUUID().toString(), "Database Design & Queries", "ER Modeling and SQL");
        Chapter dbCh1 = new Chapter(UUID.randomUUID().toString(), "Normalization & Relational Algebra", "Functional dependencies, normal forms");
        Topic dbt1 = new Topic(UUID.randomUUID().toString(), "Relational Algebra & Normalization (1NF to BCNF)", "1NF, 2NF, 3NF, BCNF decomposition and dependency preservation.");
        dbt1.setStatus("Completed");
        dbt1.setStudyMinutes(80);
        Topic dbt2 = new Topic(UUID.randomUUID().toString(), "Complex SQL Joins & Window Functions", "INNER, LEFT, FULL joins, ROW_NUMBER(), RANK(), DENSE_RANK(), PARTITION BY.");
        dbt2.setStatus("In Progress");
        dbt2.setStudyMinutes(45);
        Topic dbt3 = new Topic(UUID.randomUUID().toString(), "Transactions, Concurrency & Indexing", "ACID, 2PL, isolation levels, B+ Trees indexing.");
        dbt3.setStatus("Pending");
        dbCh1.getTopics().addAll(List.of(dbt1, dbt2, dbt3));
        dbSub1.getChapters().add(dbCh1);

        dbPath.getSubjects().add(dbSub1);
        dbPath.setLastStudiedTopicId(dbt1.getId());
        dbPath.setLastStudiedTopicTitle(dbt1.getTitle());
        dbPath.setCurrentTopicId(dbt2.getId());
        dbPath.setCurrentTopicTitle(dbt2.getTitle());
        dbPath.setNextTopicId(dbt3.getId());
        dbPath.setNextTopicTitle(dbt3.getTitle());
        dbPath.setLastStudiedAt(LocalDateTime.now().minusDays(2));
        learningService.recalculateStats(dbPath);
        learningPathRepository.save(dbPath);

        // 4. Create Initial Today's Tasks
        LocalDate today = LocalDate.now();

        Task task1 = new Task();
        task1.setUserId("user-sainath");
        task1.setTitle("DSA: Binary Tree Traversals & Diameter LeetCode Practice");
        task1.setDescription("Solve LeetCode #543 (Diameter of Binary Tree) and #102 (Level Order Traversal).");
        task1.setCategory("DSA");
        task1.setTaskType("Learning");
        task1.setDate(today);
        task1.setStartTime(LocalTime.of(9, 0));
        task1.setEndTime(LocalTime.of(10, 30));
        task1.setEstimatedDuration(90);
        task1.setPriority("High");
        task1.setStatus("Pending");
        task1.setNotes("Focus on computing depth recursively while maintaining max diameter globally.");
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setUserId("user-sainath");
        task2.setTitle("Java Backend: Spring Bean Lifecycle & Dependency Injection Lab");
        task2.setDescription("Build demo application demonstrating constructor injection and @PostConstruct hooks.");
        task2.setCategory("Java Backend");
        task2.setTaskType("Learning");
        task2.setDate(today);
        task2.setStartTime(LocalTime.of(11, 0));
        task2.setEndTime(LocalTime.of(12, 30));
        task2.setEstimatedDuration(90);
        task2.setPriority("High");
        task2.setStatus("Pending");
        taskRepository.save(task2);

        Task task3 = new Task();
        task3.setUserId("user-sainath");
        task3.setTitle("Revision: Collections Framework & HashMap Internals");
        task3.setDescription("Spaced repetition revision #2 for Java Collections and Hashing logic.");
        task3.setCategory("Java Backend");
        task3.setTaskType("Revision");
        task3.setDate(today);
        task3.setStartTime(LocalTime.of(14, 0));
        task3.setEndTime(LocalTime.of(14, 45));
        task3.setEstimatedDuration(45);
        task3.setPriority("Medium");
        task3.setStatus("Pending");
        taskRepository.save(task3);

        Task task4 = new Task();
        task4.setUserId("user-sainath");
        task4.setTitle("DBMS Lab Practical: Triggers & Cursor Implementation");
        task4.setDescription("Implement PL/SQL before/after insert triggers for college submission.");
        task4.setCategory("Practical");
        task4.setTaskType("Practical");
        task4.setDate(today);
        task4.setStartTime(LocalTime.of(15, 30));
        task4.setEndTime(LocalTime.of(17, 0));
        task4.setEstimatedDuration(90);
        task4.setPriority("High");
        task4.setStatus("Pending");
        taskRepository.save(task4);

        // Previous completed task for yesterday (to create realistic streak & stats)
        Task completedPastTask = new Task();
        completedPastTask.setUserId("user-sainath");
        completedPastTask.setTitle("DSA: Two Pointers & Dutch National Flag Algorithm");
        completedPastTask.setCategory("DSA");
        completedPastTask.setTaskType("Learning");
        completedPastTask.setDate(today.minusDays(1));
        completedPastTask.setStartTime(LocalTime.of(10, 0));
        completedPastTask.setEndTime(LocalTime.of(11, 30));
        completedPastTask.setEstimatedDuration(90);
        completedPastTask.setPriority("High");
        completedPastTask.setStatus("Completed");
        completedPastTask.setCompletedAt(today.minusDays(1).atTime(11, 30));
        taskRepository.save(completedPastTask);

        // 5. Create Revisions (Spaced Repetition)
        Revision rev1 = new Revision("user-sainath", javaPath.getId(), "Java Backend Development", jt4.getId(), "JDBC & PreparedStatement", 1, today);
        rev1.setNotes("First revision (+1 day) after completing JDBC PreparedStatement topic.");
        revisionRepository.save(rev1);

        Revision rev2 = new Revision("user-sainath", dsaPath.getId(), "DSA with C++ and LeetCode Practice", t2.getId(), "Sliding Window & Prefix Sum", 2, today.plusDays(2));
        rev2.setNotes("Second revision (+3 days) on sliding window patterns.");
        revisionRepository.save(rev2);

        Revision rev3 = new Revision("user-sainath", dbPath.getId(), "SQL and DBMS Preparation", dbt1.getId(), "Relational Algebra & Normalization", 3, today.plusDays(5));
        rev3.setNotes("Third revision (+7 days) on 3NF & BCNF decomposition.");
        revisionRepository.save(rev3);

        // 6. Create Exams
        Exam exam1 = new Exam("user-sainath", "Mid-Semester DSA Practical & Theory Exam", "Data Structures & Algorithms", today.plusDays(5), LocalTime.of(10, 0), "Arrays, Linked Lists, Stacks, Queues, Binary Trees & BST");
        exam1.setPriority("High");
        exam1.setPreparationStatus("In Progress");
        exam1.setTargetScore("95+");
        exam1.setNotes("Room 302, Engineering Block. Practice hand-drawn tree traces and code syntax.");
        examRepository.save(exam1);

        Exam exam2 = new Exam("user-sainath", "Database Management Systems Midterm", "DBMS", today.plusDays(12), LocalTime.of(14, 0), "ER Diagrams, Relational Schema, Normal Forms, SQL Joins & Subqueries");
        exam2.setPriority("High");
        exam2.setPreparationStatus("Not Started");
        exam2.setTargetScore("90+");
        examRepository.save(exam2);

        // 7. Create Assignments & Practicals
        Assignment assign1 = new Assignment("user-sainath", "DBMS Lab Practical Submission - SQL Triggers & Views", "DBMS", today.plusDays(2), "Practical");
        assign1.setSubmissionTime(LocalTime.of(23, 59));
        assign1.setDetails("Submit SQL script with automated audit triggers and materialized views demonstration.");
        assign1.setPracticalDetails("Lab batch B-2, submit hard copy and git repository URL.");
        assign1.setPriority("High");
        assign1.setStatus("In Progress");
        assignmentRepository.save(assign1);

        Assignment assign2 = new Assignment("user-sainath", "Java Backend Spring Boot Microservice Design Assignment", "Java Backend", today.plusDays(6), "Assignment");
        assign2.setSubmissionTime(LocalTime.of(18, 0));
        assign2.setDetails("Design architecture diagram and implement CRUD REST API for college portal.");
        assign2.setPriority("Medium");
        assign2.setStatus("Pending");
        assignmentRepository.save(assign2);

        // 8. Create a sample focus session
        FocusSession session1 = new FocusSession("user-sainath", task1.getId(), task1.getTitle(), "DSA", 25, "Focus");
        session1.setCompletedAt(LocalDateTime.now().minusHours(3));
        focusSessionRepository.save(session1);

        System.out.println(">>> StudySync Database Seeding Completed Successfully!");
    }
}
