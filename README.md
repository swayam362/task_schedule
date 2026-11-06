# StudySync – Smart Personal Study & Task Scheduler

**StudySync** is an intelligent personal productivity, study scheduling, and learning management system built for Computer Science Engineering students. It helps manage daily routines, college workloads, consistency across programming subjects, structured learning progress, unfinished topics, and spaced revision reminders.

---

## 🌟 Key Modules & Features

### 1. Main Application Dashboard
- **Personalized Welcome & Date Display**: Live greeting with current day, date, and local time.
- **Real-Time KPIs**: Total Tasks Today, Completed Tasks, Pending/In-Progress Tasks, Overdue/Missed Tasks.
- **Productivity Streak & Completion %**: Real database-calculated daily completion progress bar, 7-day weekly productivity summary, and daily study streak counter.
- **Today's Focus Section**: Automatically surfaces urgent, high-priority tasks with quick Pomodoro triggers.
- **Today's Hourly Timetable**: Agenda timeline displaying scheduled morning-to-night study slots.
- **Upcoming Revisions**: Spaced repetition reminders for the next 7 days.
- **Approaching Exams & Deadlines Countdown**: Live countdown for midterms, finals, and assignment submissions.

### 2. Complete Task Management System
- **Comprehensive Fields**: Title, description, category, task type, date, start time, end time, estimated duration, priority (High/Medium/Low), reminder time, notes.
- **12 Categories**: DSA, Java Backend, Python, AI/ML, SQL and DBMS, College, Assignment, Practical, Exam, Project, Personal, Other.
- **7 Task Types**: Learning, Revision, Assignment, Practical, Examination, Project, Personal activity.
- **4 Task Statuses**: Pending, In Progress, Completed, Missed.
- **Interactive Actions**: Add, edit, delete, mark completed, reschedule, start Pomodoro.
- **Filter & Sort**: Real-time filtering by category, priority, status, date range, and title search.
- **Schedule Conflict Detection**: Warns user before saving overlapping tasks on the same day, with an optional override modal.

### 3. My Learning – Progress Tracker & "Resume Where You Left Off"
- **Hierarchical Curriculum Tree**: Learning Paths &rarr; Subjects &rarr; Chapters &rarr; Topics &rarr; Subtopics.
- **Pre-Seeded Curriculums**:
  1. DSA with C++ and LeetCode Practice
  2. Java Backend Development (Core Java, Concurrency, JDBC, Spring Boot)
  3. Python Programming and AI/ML
  4. SQL and DBMS Preparation
  5. Custom User-Created Paths
- **"Resume Where You Left Off"**:
  - Displays last completed topic, last studied timestamp, current focus topic, next topic up, saved personal study notes, and course progress bar.
  - Automatically updates next sequential topic upon topic completion.
  - One-click **Continue Learning** button to launch a focused session.
  - Prompts to automatically generate Spaced Repetition revisions when a topic is finished.

### 4. Smart Revision Tracker (Spaced Repetition)
- **Spaced Intervals**: Default intervals of +1 Day, +3 Days, +7 Days, +15 Days (fully customizable in Settings).
- **Auto-Generation**: One-click spaced schedule generation linked to original topics.
- **Direct Timetable Link**: Creates scheduled revision study slots on the calendar.
- **Status Tracking**: Pending, Completed, Missed, Rescheduled.

### 5. Examination & Academic Workload Management
- **Exam Management**: Exam name, subject, exam date & time, syllabus, preparation status (Not Started, In Progress, Ready), priority, target score, notes.
- **Assignment & Practical Management**: Title, subject, submission deadline & time, assignment vs practical type, lab batch details.
- **Automated Prep Task Generator**: One-click generation of 4-day targeted study tasks leading up to examination dates.

### 6. Smart Missed Task Management
- **Automatic Detection**: Categorizes tasks whose scheduled end time has passed as Missed/Overdue without permanently deleting them.
- **Recovery Actions**: One-click Reschedule modal, Complete Late, or Cancel.

### 7. Interactive Calendar & Timetable
- **Three Views**: Monthly grid, Weekly multi-column timetable, and Daily agenda view.
- **Visual Priority Coding**: Red (High), Amber (Medium), Green (Low).
- **Click-to-Schedule**: Click any day cell or time slot to prefill and schedule tasks.

### 8. Pomodoro Focus Timer
- **Three Modes**: Focus (25m), Short Break (5m), Long Break (15m).
- **Circular Progress Ring**: SVG animated countdown with live header/sidebar mini-pill.
- **Audio Synthesizer**: Web Audio API completion chimes (no external audio files needed).
- **Task Association**: Links focus sessions to tasks, automatically recording study minutes in MongoDB and transitioning task status to "In Progress".

### 9. Productivity Analytics (Chart.js)
- **Daily Completed vs Created Tasks**: 7-day comparative bar chart.
- **Weekly Productivity Trend**: 4-week completion rate line chart.
- **Subject-Wise Study Hours**: Doughnut chart breaking down study minutes per subject.
- **Task Status Distribution**: Pie chart showing Completed, Pending, In Progress, and Missed tasks.
- **Real Metrics**: Streak, Total Study Hours, Total Sessions, Revision Consistency Rate.

### 10. Smart Daily Planner
- **Rule-Based Algorithmic Scheduler**: Evaluates available daily hours, preferred study times (morning/afternoon/evening/night), overdue missed tasks, approaching exams, and due revisions.
- **Non-Destructive**: Proposes a customized timetable that users can select, modify, and apply without overwriting existing commitments.

### 11. Notification & Reminder System
- **Web Notifications API**: Timely browser desktop alerts.
- **In-App Notification Center**: Notification bell dropdown with unread badge counter.
- **Toast Alerts**: Floating notifications for success, error, and conflict warnings.

### 12. Settings & Personalization
- **Theme**: Light Mode & Dark Mode with instant toggle and persistence.
- **Customizable Targets**: Daily study hours, preferred timing, revision intervals, Pomodoro focus/break durations, week start day.

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.4, Spring Web, Spring Data MongoDB, Spring Validation
- **Database**: MongoDB (Local port 27017, database: `studysync_db`)
- **Frontend**: HTML5, CSS3, JavaScript (ES6+), Chart.js
- **Build Tool**: Apache Maven 3.9+
- **Version Control**: Git

---

## 🚀 How to Run the Application

### Prerequisites
1. **Java 21** or later installed (`java -version`).
2. **Maven 3.9+** installed (`mvn -version`).
3. **MongoDB** service running on `localhost:27017`.

### Running with Maven
```bash
# In the project root directory
mvn spring-boot:run
```

### Running with Packaged JAR
```bash
mvn package -DskipTests
java -jar target/studysync-1.0.0.jar
```

### Accessing the Web Application
Open your web browser and navigate to:
```
http://localhost:8080
```

The application automatically seeds a default CSE student profile (`Sainath`) and rich pre-seeded learning paths (DSA, Java Backend, Python AI/ML, SQL/DBMS), tasks, exams, and revisions.

---

## 📡 REST API Summary

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/stats` | Retrieves real-time dashboard KPIs and focus tasks |
| `GET` | `/api/tasks` | Lists tasks with filters (date, category, priority, status, search) |
| `POST` | `/api/tasks` | Creates a task (checks for schedule conflict, accepts `forceOverride`) |
| `POST` | `/api/tasks/check-conflict` | Evaluates overlapping time slots |
| `PUT` | `/api/tasks/{id}` | Updates existing task |
| `PATCH` | `/api/tasks/{id}/complete` | Marks task as completed |
| `PATCH` | `/api/tasks/{id}/reschedule` | Updates date and start/end times |
| `GET` | `/api/tasks/missed` | Retrieves overdue/missed tasks |
| `GET` | `/api/learning-paths` | Retrieves all learning paths with curriculum hierarchy |
| `PATCH` | `/api/learning-paths/{id}/topics/{topicId}/status` | Updates topic status and "Resume Where You Left Off" |
| `GET` | `/api/revisions` | Retrieves revisions with filters (today, upcoming, missed, completed) |
| `POST` | `/api/revisions/schedule-topic` | Schedules spaced repetition revisions (+1d, +3d, +7d, +15d) |
| `GET` | `/api/academic/exams` | Lists upcoming examinations |
| `POST` | `/api/academic/exams/{id}/generate-tasks` | Auto-generates study preparation tasks |
| `GET` | `/api/academic/assignments` | Lists assignments and lab practicals |
| `POST` | `/api/focus-sessions` | Logs completed Pomodoro sessions |
| `GET` | `/api/analytics` | Generates metrics and Chart.js datasets |
| `GET` | `/api/planner/suggest` | Generates rule-based daily study schedule suggestions |
| `POST` | `/api/planner/apply` | Commits selected suggestions to user schedule |
| `GET/PUT` | `/api/settings` | Gets or updates user personalization preferences |

