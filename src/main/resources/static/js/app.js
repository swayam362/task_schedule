// StudySync Main Application Orchestrator
const App = {
    currentView: 'dashboard',
    userSettings: null,
    pendingConflictTask: null,

    async init() {
        this.updateHeaderDateTime();
        setInterval(() => this.updateHeaderDateTime(), 1000);

        // Load user settings first (for theme & preferences)
        await this.loadSettings();

        // Initialize sub-modules
        Notifications.init();
        Pomodoro.init();
        Planner.init();

        // Setup routing & navigation
        this.bindEvents();

        // Load initial dashboard
        await this.refreshDashboard();
    },

    updateHeaderDateTime() {
        const now = new Date();
        const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
        
        const dayEl = document.getElementById('current-day-text');
        const dateEl = document.getElementById('current-date-text');

        if (dayEl) dayEl.textContent = days[now.getDay()];
        if (dateEl) {
            const dayNum = now.getDate();
            const suffix = (dayNum === 1 || dayNum === 21 || dayNum === 31) ? 'st' : ((dayNum === 2 || dayNum === 22) ? 'nd' : ((dayNum === 3 || dayNum === 23) ? 'rd' : 'th'));
            dateEl.textContent = `${dayNum}${suffix} ${months[now.getMonth()]}, ${now.getFullYear()} • ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}`;
        }
    },

    bindEvents() {
        // Navigation clicks
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const view = item.dataset.view;
                if (view) this.switchView(view);
            });
        });

        // Theme toggle button
        const themeBtn = document.getElementById('btn-toggle-theme');
        if (themeBtn) {
            themeBtn.addEventListener('click', () => this.toggleTheme());
        }

        // Notification Bell toggle
        const notifBtn = document.getElementById('btn-toggle-notifs');
        const notifDropdown = document.getElementById('notif-dropdown-card');
        if (notifBtn && notifDropdown) {
            notifBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                notifDropdown.style.display = notifDropdown.style.display === 'block' ? 'none' : 'block';
            });
            document.addEventListener('click', (e) => {
                if (!notifDropdown.contains(e.target) && e.target !== notifBtn) {
                    notifDropdown.style.display = 'none';
                }
            });
        }

        // Mobile drawer toggle
        const mobileToggle = document.getElementById('mobile-menu-toggle');
        const sidebar = document.querySelector('.sidebar');
        if (mobileToggle && sidebar) {
            mobileToggle.addEventListener('click', () => {
                sidebar.classList.toggle('open');
            });
        }

        // Task filters
        const searchInput = document.getElementById('task-filter-search');
        const catSelect = document.getElementById('task-filter-category');
        const prioritySelect = document.getElementById('task-filter-priority');
        const statusSelect = document.getElementById('task-filter-status');
        const dateSelect = document.getElementById('task-filter-date');

        const triggerLoadTasks = () => this.loadTasks();

        if (searchInput) searchInput.addEventListener('input', () => this.debounce(triggerLoadTasks, 300)());
        if (catSelect) catSelect.addEventListener('change', triggerLoadTasks);
        if (prioritySelect) prioritySelect.addEventListener('change', triggerLoadTasks);
        if (statusSelect) statusSelect.addEventListener('change', triggerLoadTasks);
        if (dateSelect) dateSelect.addEventListener('change', triggerLoadTasks);
    },

    debounce(func, wait) {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    },

    async switchView(viewName) {
        this.currentView = viewName;

        // Update sidebar items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.toggle('active', item.dataset.view === viewName);
        });

        // Update view sections
        document.querySelectorAll('.view-section').forEach(sec => {
            sec.classList.remove('active');
        });

        const targetSec = document.getElementById(`view-${viewName}`);
        if (targetSec) targetSec.classList.add('active');

        // Close sidebar on mobile
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) sidebar.classList.remove('open');

        // Trigger module specific refresh
        if (viewName === 'dashboard') {
            await this.refreshDashboard();
        } else if (viewName === 'schedule') {
            await this.loadTasks();
        } else if (viewName === 'learning') {
            await Learning.init();
        } else if (viewName === 'revisions') {
            await this.loadRevisions();
        } else if (viewName === 'calendar') {
            await Calendar.init();
        } else if (viewName === 'exams-assignments') {
            await this.loadAcademicWorkload();
        } else if (viewName === 'completed-tasks') {
            await this.loadCompletedTasks();
        } else if (viewName === 'missed-tasks') {
            await this.loadMissedTasks();
        } else if (viewName === 'analytics') {
            await Analytics.init();
        } else if (viewName === 'settings') {
            await this.renderSettings();
        } else if (viewName === 'pomodoro') {
            Pomodoro.populateTaskDropdown();
        }
    },

    // Theme Management
    async loadSettings() {
        try {
            this.userSettings = await API.getSettings();
            if (this.userSettings.themeMode === 'dark') {
                document.documentElement.setAttribute('data-theme', 'dark');
            } else {
                document.documentElement.removeAttribute('data-theme');
            }
            this.updateThemeIcon();

            // Sync Pomodoro durations
            if (this.userSettings.focusDuration) Pomodoro.focusDuration = this.userSettings.focusDuration;
            if (this.userSettings.shortBreakDuration) Pomodoro.shortBreakDuration = this.userSettings.shortBreakDuration;
            if (this.userSettings.longBreakDuration) Pomodoro.longBreakDuration = this.userSettings.longBreakDuration;
            Pomodoro.updateSettingsFromStorage();
        } catch (e) {
            console.error('Failed to load settings:', e);
        }
    },

    async toggleTheme() {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        const newTheme = isDark ? 'light' : 'dark';
        if (newTheme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
        } else {
            document.documentElement.removeAttribute('data-theme');
        }
        this.updateThemeIcon();

        if (this.userSettings) {
            this.userSettings.themeMode = newTheme;
            try {
                await API.updateSettings(this.userSettings);
            } catch (e) {
                console.error('Failed to persist theme:', e);
            }
        }

        // Re-render charts with updated theme colors if on analytics
        if (this.currentView === 'analytics') {
            Analytics.init();
        }
    },

    updateThemeIcon() {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        const btn = document.getElementById('btn-toggle-theme');
        if (btn) {
            btn.innerHTML = isDark ? 
                '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>' :
                '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>';
        }
    },

    // Dashboard
    async refreshDashboard() {
        try {
            const stats = await API.getDashboardStats();

            // Update Metric Cards
            const totalTasksEl = document.getElementById('dash-total-tasks');
            if (totalTasksEl) totalTasksEl.textContent = stats.totalTasksToday;

            const completedTasksEl = document.getElementById('dash-completed-tasks');
            if (completedTasksEl) completedTasksEl.textContent = stats.completedTasksToday;

            const pendingTasksEl = document.getElementById('dash-pending-tasks');
            if (pendingTasksEl) pendingTasksEl.textContent = stats.pendingTasksToday;

            const missedTasksEl = document.getElementById('dash-missed-tasks');
            if (missedTasksEl) missedTasksEl.textContent = stats.overdueTasksToday;

            // Nav badges
            const missedBadge = document.getElementById('nav-missed-badge');
            if (missedBadge) {
                if (stats.overdueTasksToday > 0) {
                    missedBadge.style.display = 'inline-block';
                    missedBadge.textContent = stats.overdueTasksToday;
                } else {
                    missedBadge.style.display = 'none';
                }
            }

            // Streak & Completion
            const streakEl = document.getElementById('dash-streak-count');
            if (streakEl) streakEl.textContent = `${stats.currentStreak} Days 🔥`;

            const dailyPctEl = document.getElementById('dash-daily-pct');
            if (dailyPctEl) dailyPctEl.textContent = `${stats.dailyCompletionPercentage}%`;

            const dailyBarEl = document.getElementById('dash-daily-progress-bar');
            if (dailyBarEl) dailyBarEl.style.width = `${stats.dailyCompletionPercentage}%`;

            const weeklyRateEl = document.getElementById('dash-weekly-rate');
            if (weeklyRateEl) weeklyRateEl.textContent = `${stats.weeklyProductivitySummary}%`;

            const highPrioEl = document.getElementById('dash-high-prio-count');
            if (highPrioEl) highPrioEl.textContent = `${stats.highPriorityTasksToday} urgent`;

            const studyMinsEl = document.getElementById('dash-study-time');
            if (studyMinsEl) {
                const hrs = Math.floor(stats.totalStudyMinutesToday / 60);
                const mins = stats.totalStudyMinutesToday % 60;
                studyMinsEl.textContent = hrs > 0 ? `${hrs}h ${mins}m` : `${mins} mins`;
            }

            // Today's Focus
            this.renderTodayFocus(stats.todayFocusTasks || []);

            // Today's Hourly Timetable
            this.renderTodayTimetable(stats.todayTimetable || []);

            // Upcoming Revisions
            this.renderUpcomingRevisions(stats.upcomingRevisions || []);

            // Upcoming Exams & Assignments
            this.renderUpcomingAcademic(stats.upcomingExams || [], stats.upcomingAssignments || []);
        } catch (e) {
            console.error('Failed to load dashboard:', e);
        }
    },

    renderTodayFocus(tasks) {
        const container = document.getElementById('dash-focus-list');
        if (!container) return;

        if (tasks.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 1.5rem; color: var(--text-muted); font-size: 0.875rem;">
                    🎯 All high-priority focus tasks for today are complete! Outstanding discipline.
                </div>
            `;
            return;
        }

        container.innerHTML = tasks.map(t => `
            <div class="focus-item">
                <div class="focus-item-left">
                    <input type="checkbox" class="focus-checkbox" onchange="App.completeTaskQuick('${t.id}')">
                    <div class="focus-item-info">
                        <h4>${t.title}</h4>
                        <span>${t.startTime ? '🕒 ' + t.startTime + ' • ' : ''} ${t.estimatedDuration ? t.estimatedDuration + ' mins • ' : ''} <span class="badge ${t.priority === 'High' ? 'badge-priority-high' : 'badge-priority-medium'}">${t.priority}</span></span>
                    </div>
                </div>
                <div style="display: flex; gap: 0.5rem;">
                    <button class="btn btn-sm btn-outline-primary" onclick="App.startPomodoroForTask('${t.id}')">Focus Now</button>
                </div>
            </div>
        `).join('');
    },

    renderTodayTimetable(tasks) {
        const container = document.getElementById('dash-timetable-list');
        if (!container) return;

        if (tasks.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 2rem; color: var(--text-muted); font-size: 0.875rem;">
                    📅 No tasks scheduled for today yet. Use <strong>+ Quick Add Task</strong> or <strong>Smart Planner</strong> to organize your study session.
                </div>
            `;
            return;
        }

        container.innerHTML = tasks.map(t => {
            const isCompleted = t.status === 'Completed';
            const isMissed = t.status === 'Missed';
            return `
                <div class="timetable-slot">
                    <div class="timetable-slot-card ${isCompleted ? 'completed' : ''}">
                        <div>
                            <div class="timetable-time">${t.startTime || 'Flexible'} – ${t.endTime || ''}</div>
                            <div class="timetable-task-title" style="${isCompleted ? 'text-decoration: line-through; opacity: 0.7;' : ''}">${t.title}</div>
                            <div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.2rem;">
                                <span class="badge badge-cat">${t.category || 'General'}</span>
                                ${isCompleted ? '<span class="badge badge-priority-low">Completed</span>' : (isMissed ? '<span class="badge badge-priority-high">Overdue</span>' : '')}
                            </div>
                        </div>
                        <div style="display:flex; gap:0.35rem;">
                            ${!isCompleted ? `<button class="btn btn-sm btn-outline-primary" onclick="App.startPomodoroForTask('${t.id}')">Focus</button>` : ''}
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    renderUpcomingRevisions(revisions) {
        const container = document.getElementById('dash-revisions-list');
        if (!container) return;

        if (revisions.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:1.5rem; color:var(--text-muted); font-size:0.85rem;">
                    ✨ No pending revisions due in the next 7 days.
                </div>
            `;
            return;
        }

        container.innerHTML = revisions.map(r => `
            <div style="display:flex; align-items:center; justify-content:space-between; padding:0.75rem 0; border-bottom:1px solid var(--border-color);">
                <div>
                    <div style="font-size:0.9rem; font-weight:600; color:var(--text-primary);">${r.topicTitle}</div>
                    <div style="font-size:0.78rem; color:var(--text-muted);">${r.learningPathTitle || 'Course'} • Due: <strong>${r.scheduledDate}</strong> (Rev #${r.revisionNumber})</div>
                </div>
                <div style="display:flex; gap:0.4rem;">
                    <button class="btn btn-sm btn-outline-primary" onclick="App.completeRevisionQuick('${r.id}')">Done</button>
                </div>
            </div>
        `).join('');
    },

    renderUpcomingAcademic(exams, assignments) {
        const container = document.getElementById('dash-academic-list');
        if (!container) return;

        const items = [];
        const today = new Date();

        exams.forEach(e => {
            const examDate = new Date(e.examDate);
            const diffTime = examDate - today;
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            items.push({
                type: 'Exam',
                title: e.name,
                subtitle: `${e.subject} • ${e.examDate}`,
                daysLeft: diffDays,
                priority: e.priority,
                id: e.id
            });
        });

        assignments.forEach(a => {
            const deadline = new Date(a.submissionDeadline);
            const diffTime = deadline - today;
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            items.push({
                type: a.type || 'Assignment',
                title: a.title,
                subtitle: `${a.subject} • Due ${a.submissionDeadline}`,
                daysLeft: diffDays,
                priority: a.priority,
                id: a.id
            });
        });

        items.sort((a, b) => a.daysLeft - b.daysLeft);

        if (items.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:1.5rem; color:var(--text-muted); font-size:0.85rem;">
                    🎉 No exams or assignment deadlines in the next 14 days!
                </div>
            `;
            return;
        }

        container.innerHTML = items.slice(0, 4).map(item => `
            <div style="display:flex; align-items:center; justify-content:space-between; padding:0.75rem 0; border-bottom:1px solid var(--border-color);">
                <div>
                    <div style="font-size:0.9rem; font-weight:600; color:var(--text-primary); display:flex; align-items:center; gap:0.5rem;">
                        <span>${item.title}</span>
                        <span class="badge ${item.daysLeft <= 3 ? 'badge-priority-high' : 'badge-priority-medium'}">${item.type}</span>
                    </div>
                    <div style="font-size:0.78rem; color:var(--text-muted); margin-top:0.2rem;">${item.subtitle}</div>
                </div>
                <div style="text-align:right;">
                    <span style="font-size:0.85rem; font-weight:700; color:${item.daysLeft <= 3 ? '#ef4444' : 'var(--primary)'};">
                        ${item.daysLeft < 0 ? 'Overdue' : (item.daysLeft === 0 ? 'Today!' : `${item.daysLeft}d left`)}
                    </span>
                </div>
            </div>
        `).join('');
    },

    // Task Management Module
    async loadTasks() {
        const container = document.getElementById('tasks-container');
        if (!container) return;

        const filters = {};
        const searchInput = document.getElementById('task-filter-search');
        const catSelect = document.getElementById('task-filter-category');
        const prioritySelect = document.getElementById('task-filter-priority');
        const statusSelect = document.getElementById('task-filter-status');
        const dateSelect = document.getElementById('task-filter-date');

        if (searchInput && searchInput.value.trim()) filters.search = searchInput.value.trim();
        if (catSelect && catSelect.value) filters.category = catSelect.value;
        if (prioritySelect && prioritySelect.value) filters.priority = prioritySelect.value;
        if (statusSelect && statusSelect.value) filters.status = statusSelect.value;
        
        if (dateSelect && dateSelect.value) {
            const today = new Date();
            if (dateSelect.value === 'today') {
                filters.date = today.toISOString().split('T')[0];
            } else if (dateSelect.value === 'tomorrow') {
                const tom = new Date(today);
                tom.setDate(tom.getDate() + 1);
                filters.date = tom.toISOString().split('T')[0];
            }
        }

        try {
            const tasks = await API.getTasks(filters);
            this.renderTaskList(container, tasks);
        } catch (e) {
            console.error('Failed to load tasks:', e);
            container.innerHTML = `
                <div class="empty-state">
                    <h4>Failed to load tasks</h4>
                    <p>${e.message}</p>
                </div>
            `;
        }
    },

    renderTaskList(container, tasks) {
        if (tasks.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                    <h4>No tasks match your criteria</h4>
                    <p>Try adjusting your filters or click <strong>+ Add Task</strong> to schedule a new one.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = tasks.map(t => {
            const isCompleted = t.status === 'Completed';
            const isMissed = t.status === 'Missed';
            const pClass = t.priority === 'High' ? 'badge-priority-high' : (t.priority === 'Medium' ? 'badge-priority-medium' : 'badge-priority-low');

            return `
                <div class="task-card ${isCompleted ? 'completed' : ''}">
                    <div class="task-left">
                        <div class="task-checkbox-custom" onclick="App.toggleTaskComplete('${t.id}', '${isCompleted ? 'Pending' : 'Completed'}')">
                            ${isCompleted ? '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg>' : ''}
                        </div>
                        <div class="task-main-info">
                            <div style="display:flex; align-items:center; gap:0.5rem; flex-wrap:wrap;">
                                <span class="task-title">${t.title}</span>
                                <span class="badge ${pClass}">${t.priority}</span>
                                <span class="badge badge-cat">${t.category || 'Other'}</span>
                                ${isMissed ? '<span class="badge badge-priority-high">Overdue</span>' : ''}
                                ${isCompleted ? '<span class="badge badge-priority-low">Completed</span>' : ''}
                            </div>
                            <div class="task-meta-row">
                                <span>📅 ${t.date || 'No Date'}</span>
                                ${t.startTime ? `<span>🕒 ${t.startTime} – ${t.endTime || ''} (${t.estimatedDuration || 0}m)</span>` : ''}
                                ${t.notes ? `<span>📝 ${t.notes}</span>` : ''}
                            </div>
                        </div>
                    </div>
                    <div class="task-actions">
                        ${!isCompleted ? `<button class="btn btn-sm btn-outline-primary" onclick="App.startPomodoroForTask('${t.id}')">Focus</button>` : ''}
                        <button class="btn btn-sm btn-secondary" onclick="App.openRescheduleModal('${t.id}', '${t.date || ''}', '${t.startTime || ''}', '${t.endTime || ''}')">Reschedule</button>
                        <button class="btn btn-sm btn-secondary" onclick="App.openEditTaskModal('${t.id}')">Edit</button>
                        <button class="btn btn-sm btn-secondary" style="color:#ef4444;" onclick="App.deleteTask('${t.id}')">Delete</button>
                    </div>
                </div>
            `;
        }).join('');
    },

    // Task CRUD & Conflict Management
    openCreateTaskModal(prefillDate = null) {
        document.getElementById('task-form').reset();
        document.getElementById('task-modal-id').value = '';
        document.getElementById('task-modal-title-text').textContent = 'Create New Task';

        const todayStr = prefillDate || new Date().toISOString().split('T')[0];
        document.getElementById('task-modal-date').value = todayStr;
        document.getElementById('task-modal-start-time').value = '10:00';
        document.getElementById('task-modal-end-time').value = '11:00';

        this.openModal('modal-task-form');
    },

    async openEditTaskModal(id) {
        try {
            const task = await API.getTaskById(id);
            document.getElementById('task-modal-id').value = task.id;
            document.getElementById('task-modal-title-text').textContent = 'Edit Task';
            document.getElementById('task-modal-title').value = task.title || '';
            document.getElementById('task-modal-desc').value = task.description || '';
            document.getElementById('task-modal-category').value = task.category || 'DSA';
            document.getElementById('task-modal-type').value = task.taskType || 'Learning';
            document.getElementById('task-modal-date').value = task.date || '';
            document.getElementById('task-modal-start-time').value = task.startTime || '';
            document.getElementById('task-modal-end-time').value = task.endTime || '';
            document.getElementById('task-modal-duration').value = task.estimatedDuration || 60;
            document.getElementById('task-modal-priority').value = task.priority || 'Medium';
            document.getElementById('task-modal-reminder').value = task.reminderMinutesBefore || 10;
            document.getElementById('task-modal-notes').value = task.notes || '';

            this.openModal('modal-task-form');
        } catch (e) {
            Notifications.toast('Failed to load task details: ' + e.message, 'error');
        }
    },

    async saveTaskForm(event, forceOverride = false) {
        if (event) event.preventDefault();

        const id = document.getElementById('task-modal-id').value;
        const task = {
            title: document.getElementById('task-modal-title').value.trim(),
            description: document.getElementById('task-modal-desc').value.trim(),
            category: document.getElementById('task-modal-category').value,
            taskType: document.getElementById('task-modal-type').value,
            date: document.getElementById('task-modal-date').value || null,
            startTime: document.getElementById('task-modal-start-time').value || null,
            endTime: document.getElementById('task-modal-end-time').value || null,
            estimatedDuration: parseInt(document.getElementById('task-modal-duration').value, 10) || 60,
            priority: document.getElementById('task-modal-priority').value,
            reminderMinutesBefore: parseInt(document.getElementById('task-modal-reminder').value, 10) || 10,
            notes: document.getElementById('task-modal-notes').value.trim()
        };

        if (!task.title) {
            Notifications.toast('Task title is required.', 'error');
            return;
        }

        try {
            if (id) {
                await API.updateTask(id, task, forceOverride);
                Notifications.toast('Task updated successfully!', 'success');
            } else {
                await API.createTask(task, forceOverride);
                Notifications.toast('Task scheduled successfully!', 'success');
            }
            this.closeModal('modal-task-form');
            this.closeModal('modal-conflict-warning');
            await this.refreshDashboard();
            if (this.currentView === 'schedule') this.loadTasks();
            if (this.currentView === 'calendar') Calendar.renderCalendar();
        } catch (err) {
            if (err.isConflict) {
                // Show Conflict Warning Dialog
                this.pendingConflictTask = { id, task };
                const descEl = document.getElementById('conflict-warning-desc');
                if (descEl) descEl.textContent = err.message;
                this.openModal('modal-conflict-warning');
            } else {
                Notifications.toast('Failed to save task: ' + err.message, 'error');
            }
        }
    },

    confirmOverrideConflict() {
        if (this.pendingConflictTask) {
            const { id, task } = this.pendingConflictTask;
            if (id) {
                API.updateTask(id, task, true).then(() => {
                    Notifications.toast('Task updated with overlap override!', 'success');
                    this.closeModal('modal-conflict-warning');
                    this.closeModal('modal-task-form');
                    this.refreshDashboard();
                    if (this.currentView === 'schedule') this.loadTasks();
                    if (this.currentView === 'calendar') Calendar.renderCalendar();
                });
            } else {
                API.createTask(task, true).then(() => {
                    Notifications.toast('Task scheduled with overlap override!', 'success');
                    this.closeModal('modal-conflict-warning');
                    this.closeModal('modal-task-form');
                    this.refreshDashboard();
                    if (this.currentView === 'schedule') this.loadTasks();
                    if (this.currentView === 'calendar') Calendar.renderCalendar();
                });
            }
        }
    },

    async toggleTaskComplete(id, status) {
        try {
            await API.changeTaskStatus(id, status);
            Notifications.toast(`Task marked as ${status.toLowerCase()}!`, 'success');
            await this.refreshDashboard();
            if (this.currentView === 'schedule') this.loadTasks();
            if (this.currentView === 'completed-tasks') this.loadCompletedTasks();
        } catch (e) {
            Notifications.toast('Failed to update task: ' + e.message, 'error');
        }
    },

    async completeTaskQuick(id) {
        try {
            await API.markTaskCompleted(id);
            Notifications.toast('Task completed! Statistics updated.', 'success');
            await this.refreshDashboard();
            if (this.currentView === 'schedule') this.loadTasks();
        } catch (e) {
            Notifications.toast('Failed to complete task: ' + e.message, 'error');
        }
    },

    async deleteTask(id) {
        if (!confirm('Are you sure you want to permanently delete this task?')) return;
        try {
            await API.deleteTask(id);
            Notifications.toast('Task deleted.', 'info');
            await this.refreshDashboard();
            if (this.currentView === 'schedule') this.loadTasks();
            if (this.currentView === 'missed-tasks') this.loadMissedTasks();
        } catch (e) {
            Notifications.toast('Failed to delete task: ' + e.message, 'error');
        }
    },

    openRescheduleModal(id, currentDate, currentStart, currentEnd) {
        document.getElementById('reschedule-task-id').value = id;
        document.getElementById('reschedule-date').value = currentDate || new Date().toISOString().split('T')[0];
        document.getElementById('reschedule-start-time').value = currentStart || '10:00';
        document.getElementById('reschedule-end-time').value = currentEnd || '11:00';
        this.openModal('modal-reschedule-task');
    },

    async submitReschedule() {
        const id = document.getElementById('reschedule-task-id').value;
        const date = document.getElementById('reschedule-date').value;
        const startTime = document.getElementById('reschedule-start-time').value;
        const endTime = document.getElementById('reschedule-end-time').value;

        if (!date) {
            Notifications.toast('Please select a date.', 'error');
            return;
        }

        try {
            await API.rescheduleTask(id, date, startTime, endTime);
            Notifications.toast(`Task rescheduled to ${date}!`, 'success');
            this.closeModal('modal-reschedule-task');
            await this.refreshDashboard();
            if (this.currentView === 'schedule') this.loadTasks();
            if (this.currentView === 'missed-tasks') this.loadMissedTasks();
        } catch (e) {
            Notifications.toast('Failed to reschedule task: ' + e.message, 'error');
        }
    },

    // Missed Tasks Module
    async loadMissedTasks() {
        const container = document.getElementById('missed-tasks-container');
        if (!container) return;

        try {
            const tasks = await API.getMissedTasks();
            if (tasks.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 14 14"></polyline></svg>
                        <h4>No missed tasks!</h4>
                        <p>You have no overdue or missed tasks. Keep up the high consistency!</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = tasks.map(t => `
                <div class="task-card" style="border-left: 4px solid #ef4444;">
                    <div class="task-left">
                        <div class="task-main-info">
                            <div style="display:flex; align-items:center; gap:0.5rem;">
                                <span class="task-title">${t.title}</span>
                                <span class="badge badge-priority-high">Missed / Overdue</span>
                                <span class="badge badge-cat">${t.category || 'Other'}</span>
                            </div>
                            <div class="task-meta-row">
                                <span>Originally Scheduled: <strong>${t.date || 'Past'} ${t.startTime || ''}</strong></span>
                                ${t.notes ? `<span>📝 ${t.notes}</span>` : ''}
                            </div>
                        </div>
                    </div>
                    <div class="task-actions">
                        <button class="btn btn-sm btn-primary" onclick="App.openRescheduleModal('${t.id}', '', '${t.startTime || ''}', '${t.endTime || ''}')">Reschedule</button>
                        <button class="btn btn-sm btn-secondary" onclick="App.toggleTaskComplete('${t.id}', 'Completed')">Complete Late</button>
                        <button class="btn btn-sm btn-secondary" style="color:#ef4444;" onclick="App.deleteTask('${t.id}')">Cancel</button>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            console.error('Failed to load missed tasks:', e);
        }
    },

    // Completed Tasks Module
    async loadCompletedTasks() {
        const container = document.getElementById('completed-tasks-container');
        if (!container) return;

        try {
            const tasks = await API.getTasks({ status: 'Completed' });
            if (tasks.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        <h4>No completed tasks yet</h4>
                        <p>Complete tasks on your schedule to track your learning victories here.</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = tasks.map(t => `
                <div class="task-card completed">
                    <div class="task-left">
                        <div class="task-main-info">
                            <div style="display:flex; align-items:center; gap:0.5rem;">
                                <span class="task-title">${t.title}</span>
                                <span class="badge badge-priority-low">Completed</span>
                                <span class="badge badge-cat">${t.category || 'Other'}</span>
                            </div>
                            <div class="task-meta-row">
                                <span>Completed on: ${t.completedAt ? new Date(t.completedAt).toLocaleDateString() : t.date}</span>
                                <span>Duration: ${t.estimatedDuration || 0} mins</span>
                            </div>
                        </div>
                    </div>
                    <div class="task-actions">
                        <button class="btn btn-sm btn-secondary" onclick="App.toggleTaskComplete('${t.id}', 'Pending')">Re-open</button>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            console.error('Failed to load completed tasks:', e);
        }
    },

    // Revision Tracker Module
    async loadRevisions(filter = 'all') {
        const container = document.getElementById('revisions-container');
        if (!container) return;

        document.querySelectorAll('.rev-filter-btn').forEach(b => {
            b.classList.toggle('active', b.dataset.filter === filter);
        });

        try {
            const revisions = await API.getRevisions(filter);
            if (revisions.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M21 2v6h-6"></path><path d="M3 12a9 9 0 0 1 15-6.7L21 8"></path><path d="M3 22v-6h6"></path><path d="M21 12a9 9 0 0 1-15 6.7L3 16"></path></svg>
                        <h4>No revisions found</h4>
                        <p>Complete topics in <strong>My Learning</strong> to auto-schedule spaced repetitions.</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = revisions.map(r => {
                const isCompleted = r.status === 'Completed';
                const isMissed = r.status === 'Missed';
                return `
                    <div class="task-card ${isCompleted ? 'completed' : ''}">
                        <div class="task-left">
                            <div class="task-main-info">
                                <div style="display:flex; align-items:center; gap:0.5rem;">
                                    <span class="task-title">${r.topicTitle}</span>
                                    <span class="badge badge-cat">${r.learningPathTitle || 'Subject'}</span>
                                    <span class="badge ${isCompleted ? 'badge-priority-low' : (isMissed ? 'badge-priority-high' : 'badge-priority-medium')}">
                                        Revision #${r.revisionNumber} (${r.status})
                                    </span>
                                </div>
                                <div class="task-meta-row">
                                    <span>📅 Scheduled: <strong>${r.scheduledDate}</strong></span>
                                    ${r.notes ? `<span>📝 ${r.notes}</span>` : ''}
                                </div>
                            </div>
                        </div>
                        <div class="task-actions">
                            ${!isCompleted ? `
                                <button class="btn btn-sm btn-primary" onclick="App.completeRevisionQuick('${r.id}')">Mark Done</button>
                                <button class="btn btn-sm btn-secondary" onclick="App.openRescheduleRevModal('${r.id}', '${r.scheduledDate}')">Reschedule</button>
                            ` : '<span style="font-size:0.8rem; color:var(--primary); font-weight:600;">✓ Retained</span>'}
                            <button class="btn btn-sm btn-secondary" style="color:#ef4444;" onclick="App.deleteRevision('${r.id}')">Delete</button>
                        </div>
                    </div>
                `;
            }).join('');
        } catch (e) {
            console.error('Failed to load revisions:', e);
        }
    },

    async completeRevisionQuick(id) {
        try {
            await API.markRevisionCompleted(id);
            Notifications.toast('Revision completed! Memory retention refreshed.', 'success');
            await this.refreshDashboard();
            if (this.currentView === 'revisions') this.loadRevisions();
        } catch (e) {
            Notifications.toast('Failed to complete revision: ' + e.message, 'error');
        }
    },

    openRescheduleRevModal(id, currentDate) {
        document.getElementById('reschedule-rev-id').value = id;
        document.getElementById('reschedule-rev-date').value = currentDate || '';
        this.openModal('modal-reschedule-rev');
    },

    async submitRescheduleRev() {
        const id = document.getElementById('reschedule-rev-id').value;
        const date = document.getElementById('reschedule-rev-date').value;
        if (!date) return;

        try {
            await API.rescheduleRevision(id, date);
            Notifications.toast(`Revision rescheduled to ${date}!`, 'success');
            this.closeModal('modal-reschedule-rev');
            this.loadRevisions();
        } catch (e) {
            Notifications.toast('Failed to reschedule: ' + e.message, 'error');
        }
    },

    async deleteRevision(id) {
        if (!confirm('Delete this revision reminder?')) return;
        try {
            await API.deleteRevision(id);
            Notifications.toast('Revision deleted.', 'info');
            this.loadRevisions();
        } catch (e) {
            Notifications.toast('Failed to delete: ' + e.message, 'error');
        }
    },

    // Academic Workload (Exams & Assignments)
    async loadAcademicWorkload() {
        const examsContainer = document.getElementById('exams-list-container');
        const assignContainer = document.getElementById('assignments-list-container');

        try {
            const exams = await API.getExams();
            const assignments = await API.getAssignments();

            // Render Exams
            if (examsContainer) {
                if (exams.length === 0) {
                    examsContainer.innerHTML = '<p style="color:var(--text-muted); font-size:0.875rem;">No upcoming exams added. Click + Add Exam to track syllabus.</p>';
                } else {
                    examsContainer.innerHTML = exams.map(e => `
                        <div class="card" style="margin-bottom:1rem;">
                            <div class="card-body">
                                <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:0.5rem;">
                                    <div>
                                        <h4 style="font-size:1.1rem; font-weight:700;">${e.name}</h4>
                                        <span style="font-size:0.85rem; color:var(--text-muted);">${e.subject} • Date: <strong>${e.examDate} ${e.examTime || ''}</strong></span>
                                    </div>
                                    <span class="badge ${e.priority === 'High' ? 'badge-priority-high' : 'badge-priority-medium'}">${e.priority}</span>
                                </div>
                                <p style="font-size:0.85rem; color:var(--text-secondary); margin:0.5rem 0;"><strong>Syllabus:</strong> ${e.syllabus || 'Not specified'}</p>
                                ${e.notes ? `<p style="font-size:0.8rem; color:var(--text-muted); margin-bottom:0.75rem;">📝 ${e.notes}</p>` : ''}
                                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.75rem; pt:0.5rem; border-top:1px solid var(--border-color);">
                                    <span style="font-size:0.8rem; font-weight:600; color:var(--primary);">Prep Status: ${e.preparationStatus || 'Not Started'}</span>
                                    <div style="display:flex; gap:0.5rem;">
                                        <button class="btn btn-sm btn-outline-primary" onclick="App.generateExamTasks('${e.id}')">Generate Prep Tasks</button>
                                        <button class="btn btn-sm btn-secondary" style="color:#ef4444;" onclick="App.deleteExam('${e.id}')">Delete</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `).join('');
                }
            }

            // Render Assignments & Practicals
            if (assignContainer) {
                if (assignments.length === 0) {
                    assignContainer.innerHTML = '<p style="color:var(--text-muted); font-size:0.875rem;">No assignments or practicals added. Click + Add Assignment to track college workload.</p>';
                } else {
                    assignContainer.innerHTML = assignments.map(a => `
                        <div class="card" style="margin-bottom:1rem;">
                            <div class="card-body">
                                <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:0.5rem;">
                                    <div>
                                        <h4 style="font-size:1.05rem; font-weight:700;">${a.title}</h4>
                                        <span style="font-size:0.85rem; color:var(--text-muted);">${a.subject} • Deadline: <strong>${a.submissionDeadline} ${a.submissionTime || ''}</strong></span>
                                    </div>
                                    <span class="badge badge-cat">${a.type || 'Assignment'}</span>
                                </div>
                                <p style="font-size:0.85rem; color:var(--text-secondary); margin:0.4rem 0;">${a.details || ''}</p>
                                ${a.practicalDetails ? `<p style="font-size:0.8rem; color:var(--primary-text); background:var(--primary-light); padding:0.35rem 0.6rem; border-radius:4px;">🔬 Lab: ${a.practicalDetails}</p>` : ''}
                                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.75rem;">
                                    <span class="badge ${a.status === 'Submitted' ? 'badge-priority-low' : 'badge-priority-medium'}">${a.status || 'Pending'}</span>
                                    <div style="display:flex; gap:0.5rem;">
                                        <button class="btn btn-sm btn-outline-primary" onclick="App.generateAssignmentTask('${a.id}')">Add Work Slot</button>
                                        <button class="btn btn-sm btn-secondary" style="color:#ef4444;" onclick="App.deleteAssignment('${a.id}')">Delete</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `).join('');
                }
            }
        } catch (e) {
            console.error('Failed to load academic workload:', e);
        }
    },

    async generateExamTasks(examId) {
        try {
            const tasks = await API.generateExamTasks(examId, 4, 2);
            Notifications.toast(`Generated ${tasks.length} targeted study preparation tasks for your exam!`, 'success');
            this.loadAcademicWorkload();
            this.refreshDashboard();
        } catch (e) {
            Notifications.toast('Failed to generate prep tasks: ' + e.message, 'error');
        }
    },

    async generateAssignmentTask(assignId) {
        try {
            await API.generateAssignmentTask(assignId);
            Notifications.toast('Added dedicated work slot for assignment to your schedule!', 'success');
            this.refreshDashboard();
        } catch (e) {
            Notifications.toast('Failed to generate slot: ' + e.message, 'error');
        }
    },

    async deleteExam(id) {
        if (!confirm('Delete this exam record?')) return;
        try {
            await API.deleteExam(id);
            Notifications.toast('Exam deleted.', 'info');
            this.loadAcademicWorkload();
        } catch (e) {
            Notifications.toast('Failed to delete: ' + e.message, 'error');
        }
    },

    async deleteAssignment(id) {
        if (!confirm('Delete this assignment record?')) return;
        try {
            await API.deleteAssignment(id);
            Notifications.toast('Assignment deleted.', 'info');
            this.loadAcademicWorkload();
        } catch (e) {
            Notifications.toast('Failed to delete: ' + e.message, 'error');
        }
    },

    openAddExamModal() {
        document.getElementById('exam-form').reset();
        this.openModal('modal-add-exam');
    },

    async saveExamForm(event) {
        event.preventDefault();
        const exam = {
            name: document.getElementById('exam-modal-name').value.trim(),
            subject: document.getElementById('exam-modal-subject').value.trim(),
            examDate: document.getElementById('exam-modal-date').value,
            examTime: document.getElementById('exam-modal-time').value || '10:00',
            syllabus: document.getElementById('exam-modal-syllabus').value.trim(),
            priority: document.getElementById('exam-modal-priority').value,
            targetScore: document.getElementById('exam-modal-target').value.trim(),
            notes: document.getElementById('exam-modal-notes').value.trim()
        };

        if (!exam.name || !exam.examDate) {
            Notifications.toast('Exam name and date are required.', 'error');
            return;
        }

        try {
            await API.createExam(exam);
            Notifications.toast('Exam added to academic workload!', 'success');
            this.closeModal('modal-add-exam');
            this.loadAcademicWorkload();
            this.refreshDashboard();
        } catch (e) {
            Notifications.toast('Failed to save exam: ' + e.message, 'error');
        }
    },

    openAddAssignmentModal() {
        document.getElementById('assignment-form').reset();
        this.openModal('modal-add-assignment');
    },

    async saveAssignmentForm(event) {
        event.preventDefault();
        const assign = {
            title: document.getElementById('assign-modal-title').value.trim(),
            subject: document.getElementById('assign-modal-subject').value.trim(),
            submissionDeadline: document.getElementById('assign-modal-deadline').value,
            submissionTime: document.getElementById('assign-modal-time').value || '23:59',
            type: document.getElementById('assign-modal-type').value,
            details: document.getElementById('assign-modal-details').value.trim(),
            practicalDetails: document.getElementById('assign-modal-practical').value.trim(),
            priority: document.getElementById('assign-modal-priority').value
        };

        if (!assign.title || !assign.submissionDeadline) {
            Notifications.toast('Title and deadline are required.', 'error');
            return;
        }

        try {
            await API.createAssignment(assign);
            Notifications.toast('Assignment added successfully!', 'success');
            this.closeModal('modal-add-assignment');
            this.loadAcademicWorkload();
            this.refreshDashboard();
        } catch (e) {
            Notifications.toast('Failed to save assignment: ' + e.message, 'error');
        }
    },

    // Pomodoro bridge
    startPomodoroForTask(taskId) {
        API.getTaskById(taskId).then(task => {
            Pomodoro.selectTaskDirectly(task.id, task.title, task.category);
            this.switchView('pomodoro');
            Notifications.toast(`Pomodoro focus session ready for "${task.title}".`, 'success');
        });
    },

    // Settings
    async renderSettings() {
        if (!this.userSettings) await this.loadSettings();
        const s = this.userSettings;

        const studyHoursInput = document.getElementById('setting-study-hours');
        const preferredTimeInput = document.getElementById('setting-preferred-time');
        const intervalsInput = document.getElementById('setting-rev-intervals');
        const notifSoundInput = document.getElementById('setting-notif-sound');
        const browserNotifInput = document.getElementById('setting-browser-notif');
        const reminderLeadInput = document.getElementById('setting-reminder-lead');
        const weekStartInput = document.getElementById('setting-week-start');
        const focusDurationInput = document.getElementById('setting-pomo-focus');
        const shortBreakInput = document.getElementById('setting-pomo-short');
        const longBreakInput = document.getElementById('setting-pomo-long');

        if (studyHoursInput) studyHoursInput.value = s.dailyStudyHours || 6;
        if (preferredTimeInput) preferredTimeInput.value = s.preferredStudyTime || 'Morning';
        if (intervalsInput) intervalsInput.value = (s.revisionIntervals || [1, 3, 7, 15]).join(', ');
        if (notifSoundInput) notifSoundInput.checked = s.notificationSound !== false;
        if (browserNotifInput) browserNotifInput.checked = s.browserNotificationsEnabled !== false;
        if (reminderLeadInput) reminderLeadInput.value = s.reminderMinutesBefore || 10;
        if (weekStartInput) weekStartInput.value = s.weekStartDay || 'Monday';
        if (focusDurationInput) focusDurationInput.value = s.focusDuration || 25;
        if (shortBreakInput) shortBreakInput.value = s.shortBreakDuration || 5;
        if (longBreakInput) longBreakInput.value = s.longBreakDuration || 15;
    },

    async saveSettingsForm(event) {
        event.preventDefault();
        const intervalsStr = document.getElementById('setting-rev-intervals').value;
        const intervals = intervalsStr.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n));

        const updated = {
            ...this.userSettings,
            dailyStudyHours: parseInt(document.getElementById('setting-study-hours').value, 10) || 6,
            preferredStudyTime: document.getElementById('setting-preferred-time').value,
            revisionIntervals: intervals.length > 0 ? intervals : [1, 3, 7, 15],
            notificationSound: document.getElementById('setting-notif-sound').checked,
            browserNotificationsEnabled: document.getElementById('setting-browser-notif').checked,
            reminderMinutesBefore: parseInt(document.getElementById('setting-reminder-lead').value, 10) || 10,
            weekStartDay: document.getElementById('setting-week-start').value,
            focusDuration: parseInt(document.getElementById('setting-pomo-focus').value, 10) || 25,
            shortBreakDuration: parseInt(document.getElementById('setting-pomo-short').value, 10) || 5,
            longBreakDuration: parseInt(document.getElementById('setting-pomo-long').value, 10) || 15
        };

        try {
            this.userSettings = await API.updateSettings(updated);
            Notifications.toast('Settings saved successfully!', 'success');
            Pomodoro.focusDuration = this.userSettings.focusDuration;
            Pomodoro.shortBreakDuration = this.userSettings.shortBreakDuration;
            Pomodoro.longBreakDuration = this.userSettings.longBreakDuration;
            Pomodoro.updateSettingsFromStorage();
        } catch (e) {
            Notifications.toast('Failed to save settings: ' + e.message, 'error');
        }
    },

    // Modal Helpers
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.add('active');
    },

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.remove('active');
    }
};

// Launch when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
