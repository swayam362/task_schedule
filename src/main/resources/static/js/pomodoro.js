// StudySync Pomodoro Focus Timer
const Pomodoro = {
    mode: 'focus', // 'focus', 'shortBreak', 'longBreak'
    durationMinutes: 25,
    remainingSeconds: 25 * 60,
    timerInterval: null,
    isRunning: false,
    selectedTaskId: null,
    selectedTaskTitle: 'General Focus',
    selectedCategory: 'DSA',

    // Settings default
    focusDuration: 25,
    shortBreakDuration: 5,
    longBreakDuration: 15,

    init() {
        this.updateSettingsFromStorage();
        this.setMode('focus');
        this.populateTaskDropdown();
    },

    updateSettingsFromStorage() {
        // Can be updated by Settings module
        this.remainingSeconds = this.focusDuration * 60;
        this.updateDisplay();
    },

    setMode(mode) {
        this.pause();
        this.mode = mode;

        if (mode === 'focus') {
            this.durationMinutes = this.focusDuration;
        } else if (mode === 'shortBreak') {
            this.durationMinutes = this.shortBreakDuration;
        } else if (mode === 'longBreak') {
            this.durationMinutes = this.longBreakDuration;
        }

        this.remainingSeconds = this.durationMinutes * 60;
        this.updateModeTabs();
        this.updateDisplay();
    },

    updateModeTabs() {
        document.querySelectorAll('.pomo-mode-tab').forEach(tab => {
            tab.classList.toggle('active', tab.dataset.mode === this.mode);
        });

        const label = document.getElementById('timer-session-label');
        if (label) {
            label.textContent = this.mode === 'focus' ? 'Deep Work Focus' : (this.mode === 'shortBreak' ? 'Short Recovery Break' : 'Restorative Long Break');
        }
    },

    toggle() {
        if (this.isRunning) {
            this.pause();
        } else {
            this.start();
        }
    },

    start() {
        if (this.isRunning) return;
        this.isRunning = true;
        this.updatePlayBtn(true);

        this.timerInterval = setInterval(() => {
            if (this.remainingSeconds > 0) {
                this.remainingSeconds--;
                this.updateDisplay();
            } else {
                this.completeSession();
            }
        }, 1000);
    },

    pause() {
        this.isRunning = false;
        clearInterval(this.timerInterval);
        this.updatePlayBtn(false);
    },

    reset() {
        this.pause();
        this.remainingSeconds = this.durationMinutes * 60;
        this.updateDisplay();
    },

    skip() {
        this.pause();
        if (this.mode === 'focus') {
            this.setMode('shortBreak');
        } else {
            this.setMode('focus');
        }
    },

    updatePlayBtn(running) {
        const btn = document.getElementById('btn-timer-toggle');
        if (btn) {
            btn.innerHTML = running ? 
                '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>' :
                '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>';
        }
    },

    updateDisplay() {
        const mins = Math.floor(this.remainingSeconds / 60);
        const secs = this.remainingSeconds % 60;
        const timeFormatted = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

        // Circular progress ring
        const totalSecs = this.durationMinutes * 60;
        const progressFraction = totalSecs > 0 ? (totalSecs - this.remainingSeconds) / totalSecs : 0;
        const circle = document.getElementById('timer-progress-circle');
        if (circle) {
            const circumference = 2 * Math.PI * 110; // r=110
            circle.style.strokeDasharray = `${circumference} ${circumference}`;
            circle.style.strokeDashoffset = circumference * (1 - progressFraction);
        }

        const mainCount = document.getElementById('timer-countdown');
        if (mainCount) mainCount.textContent = timeFormatted;

        // Mini sidebar pill
        const pillTime = document.getElementById('sidebar-pomo-time');
        if (pillTime) pillTime.textContent = timeFormatted;

        const pillLabel = document.getElementById('sidebar-pomo-label');
        if (pillLabel) pillLabel.textContent = this.isRunning ? (this.mode === 'focus' ? 'Focusing...' : 'Break...') : 'Ready';

        // Update document title if running
        if (this.isRunning) {
            document.title = `(${timeFormatted}) StudySync Focus`;
        } else {
            document.title = 'StudySync – Smart Study Scheduler';
        }
    },

    async completeSession() {
        this.pause();
        Notifications.playChime();

        if (this.mode === 'focus') {
            Notifications.toast(`Great job! Completed ${this.durationMinutes} minutes focus session.`, 'success');
            Notifications.sendNativeNotification('Focus Session Complete', `Awesome work! Completed ${this.durationMinutes}m focus on ${this.selectedTaskTitle}.`);

            // Persist to MongoDB
            try {
                await API.recordFocusSession({
                    taskId: this.selectedTaskId,
                    taskTitle: this.selectedTaskTitle,
                    category: this.selectedCategory,
                    durationMinutes: this.durationMinutes,
                    sessionType: 'Focus'
                });
                // Refresh dashboard stats
                if (window.App && typeof App.refreshDashboard === 'function') {
                    App.refreshDashboard();
                }
            } catch (err) {
                console.error('Failed to log focus session:', err);
            }

            // Switch to break
            this.setMode('shortBreak');
        } else {
            Notifications.toast('Break over! Ready to get back into focus mode?', 'info');
            this.setMode('focus');
        }
    },

    async populateTaskDropdown() {
        const select = document.getElementById('pomo-task-select');
        if (!select) return;

        try {
            const todayStr = new Date().toISOString().split('T')[0];
            const tasks = await API.getTasks({ date: todayStr });
            select.innerHTML = '<option value="">-- General Study Focus --</option>';
            
            tasks.filter(t => t.status !== 'Completed').forEach(t => {
                const opt = document.createElement('option');
                opt.value = t.id;
                opt.dataset.title = t.title;
                opt.dataset.category = t.category || 'Other';
                opt.textContent = `[${t.category || 'Task'}] ${t.title}`;
                select.appendChild(opt);
            });

            select.onchange = (e) => {
                const opt = select.selectedOptions[0];
                if (opt && opt.value) {
                    this.selectedTaskId = opt.value;
                    this.selectedTaskTitle = opt.dataset.title;
                    this.selectedCategory = opt.dataset.category;
                } else {
                    this.selectedTaskId = null;
                    this.selectedTaskTitle = 'General Focus';
                    this.selectedCategory = 'DSA';
                }
                const activeTaskDisplay = document.getElementById('pomo-active-task-name');
                if (activeTaskDisplay) activeTaskDisplay.textContent = this.selectedTaskTitle;
            };
        } catch (e) {
            console.error('Failed to load tasks for pomodoro select', e);
        }
    },

    selectTaskDirectly(taskId, taskTitle, category) {
        this.selectedTaskId = taskId;
        this.selectedTaskTitle = taskTitle;
        this.selectedCategory = category || 'Other';

        const select = document.getElementById('pomo-task-select');
        if (select) select.value = taskId;

        const activeTaskDisplay = document.getElementById('pomo-active-task-name');
        if (activeTaskDisplay) activeTaskDisplay.textContent = taskTitle;

        this.setMode('focus');
    }
};
