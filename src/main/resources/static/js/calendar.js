// StudySync Interactive Calendar Engine
const Calendar = {
    currentDate: new Date(),
    viewMode: 'month', // 'month', 'week', 'day'
    tasksMap: new Map(), // 'YYYY-MM-DD' -> array of tasks

    init() {
        this.renderCalendar();
    },

    async loadTasksForRange() {
        try {
            // Load all tasks for the current user
            const tasks = await API.getTasks();
            this.tasksMap.clear();

            tasks.forEach(t => {
                if (t.date) {
                    if (!this.tasksMap.has(t.date)) {
                        this.tasksMap.set(t.date, []);
                    }
                    this.tasksMap.get(t.date).push(t);
                }
            });
        } catch (e) {
            console.error('Failed to load calendar tasks:', e);
        }
    },

    async setViewMode(mode) {
        this.viewMode = mode;
        document.querySelectorAll('.cal-mode-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mode === mode);
        });
        await this.renderCalendar();
    },

    prev() {
        if (this.viewMode === 'month') {
            this.currentDate.setMonth(this.currentDate.getMonth() - 1);
        } else if (this.viewMode === 'week') {
            this.currentDate.setDate(this.currentDate.getDate() - 7);
        } else {
            this.currentDate.setDate(this.currentDate.getDate() - 1);
        }
        this.renderCalendar();
    },

    next() {
        if (this.viewMode === 'month') {
            this.currentDate.setMonth(this.currentDate.getMonth() + 1);
        } else if (this.viewMode === 'week') {
            this.currentDate.setDate(this.currentDate.getDate() + 7);
        } else {
            this.currentDate.setDate(this.currentDate.getDate() + 1);
        }
        this.renderCalendar();
    },

    goToToday() {
        this.currentDate = new Date();
        this.renderCalendar();
    },

    async renderCalendar() {
        await this.loadTasksForRange();

        const titleEl = document.getElementById('calendar-period-title');
        const container = document.getElementById('calendar-container');
        if (!container) return;

        if (this.viewMode === 'month') {
            this.renderMonthView(container, titleEl);
        } else if (this.viewMode === 'week') {
            this.renderWeekView(container, titleEl);
        } else {
            this.renderDayView(container, titleEl);
        }
    },

    renderMonthView(container, titleEl) {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        
        if (titleEl) {
            titleEl.textContent = `${monthNames[month]} ${year}`;
        }

        const firstDayIndex = new Date(year, month, 1).getDay(); // 0 is Sun
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const todayStr = new Date().toISOString().split('T')[0];

        let html = `
            <div class="calendar-grid">
                <div class="calendar-day-header">Sun</div>
                <div class="calendar-day-header">Mon</div>
                <div class="calendar-day-header">Tue</div>
                <div class="calendar-day-header">Wed</div>
                <div class="calendar-day-header">Thu</div>
                <div class="calendar-day-header">Fri</div>
                <div class="calendar-day-header">Sat</div>
        `;

        // Empty cells for offset
        for (let i = 0; i < firstDayIndex; i++) {
            html += `<div class="calendar-day-cell" style="background: var(--bg-card-subtle); opacity: 0.5;"></div>`;
        }

        // Days of month
        for (let day = 1; day <= daysInMonth; day++) {
            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const isToday = dateStr === todayStr;
            const tasks = this.tasksMap.get(dateStr) || [];

            html += `
                <div class="calendar-day-cell ${isToday ? 'today' : ''}" onclick="Calendar.handleDayClick('${dateStr}')">
                    <div class="calendar-day-num">${day} ${isToday ? '<span style="color:var(--primary); font-size:0.75rem;">(Today)</span>' : ''}</div>
                    <div class="calendar-day-tasks">
                        ${tasks.slice(0, 3).map(t => {
                            const pClass = t.priority === 'High' ? 'badge-priority-high' : (t.priority === 'Medium' ? 'badge-priority-medium' : 'badge-priority-low');
                            return `<div class="cal-task-pill ${pClass}" title="${t.title} (${t.startTime || ''})">${t.startTime ? t.startTime + ' ' : ''}${t.title}</div>`;
                        }).join('')}
                        ${tasks.length > 3 ? `<div style="font-size:0.7rem; color:var(--text-muted); font-weight:600;">+${tasks.length - 3} more</div>` : ''}
                    </div>
                </div>
            `;
        }

        html += `</div>`;
        container.innerHTML = html;
    },

    renderWeekView(container, titleEl) {
        const startOfWeek = new Date(this.currentDate);
        startOfWeek.setDate(this.currentDate.getDate() - this.currentDate.getDay()); // Sunday

        const endOfWeek = new Date(startOfWeek);
        endOfWeek.setDate(startOfWeek.getDate() + 6);

        if (titleEl) {
            titleEl.textContent = `${startOfWeek.toLocaleDateString('default', { month: 'short', day: 'numeric' })} – ${endOfWeek.toLocaleDateString('default', { month: 'short', day: 'numeric', year: 'numeric' })}`;
        }

        let html = `<div class="calendar-grid">`;
        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        days.forEach(d => html += `<div class="calendar-day-header">${d}</div>`);

        for (let i = 0; i < 7; i++) {
            const d = new Date(startOfWeek);
            d.setDate(startOfWeek.getDate() + i);
            const dateStr = d.toISOString().split('T')[0];
            const tasks = this.tasksMap.get(dateStr) || [];
            const isToday = dateStr === new Date().toISOString().split('T')[0];

            html += `
                <div class="calendar-day-cell ${isToday ? 'today' : ''}" style="min-height: 220px;" onclick="Calendar.handleDayClick('${dateStr}')">
                    <div class="calendar-day-num" style="display:flex; justify-content:space-between;">
                        <span>${d.getDate()}</span>
                        <span style="font-size:0.75rem; color:var(--text-muted);">${days[i]}</span>
                    </div>
                    <div class="calendar-day-tasks">
                        ${tasks.map(t => {
                            const pClass = t.priority === 'High' ? 'badge-priority-high' : (t.priority === 'Medium' ? 'badge-priority-medium' : 'badge-priority-low');
                            return `<div class="cal-task-pill ${pClass}" style="margin-bottom:4px;" title="${t.title}">${t.startTime ? '<strong>' + t.startTime + '</strong> ' : ''}${t.title}</div>`;
                        }).join('')}
                    </div>
                </div>
            `;
        }
        html += `</div>`;
        container.innerHTML = html;
    },

    renderDayView(container, titleEl) {
        const dateStr = this.currentDate.toISOString().split('T')[0];
        const dayFormatted = this.currentDate.toLocaleDateString('default', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
        
        if (titleEl) {
            titleEl.textContent = dayFormatted;
        }

        const tasks = this.tasksMap.get(dateStr) || [];

        let html = `
            <div style="background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 1.5rem;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.25rem;">
                    <h3 style="font-size:1.1rem; font-weight:700;">Scheduled for ${dayFormatted} (${tasks.length} tasks)</h3>
                    <button class="btn btn-sm btn-primary" onclick="App.openCreateTaskModal('${dateStr}')">+ Add Task For This Day</button>
                </div>
                ${tasks.length === 0 ? `
                    <div class="empty-state">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                        <h4>No tasks scheduled for this day</h4>
                        <p>Enjoy your free time or schedule a study session to get ahead.</p>
                    </div>
                ` : `
                    <div class="task-list">
                        ${tasks.map(t => `
                            <div class="task-card">
                                <div class="task-left">
                                    <div class="task-main-info">
                                        <div style="display:flex; align-items:center; gap:0.5rem;">
                                            <span class="task-title">${t.title}</span>
                                            <span class="badge ${t.priority === 'High' ? 'badge-priority-high' : (t.priority === 'Medium' ? 'badge-priority-medium' : 'badge-priority-low')}">${t.priority}</span>
                                            <span class="badge badge-cat">${t.category}</span>
                                        </div>
                                        <div class="task-meta-row">
                                            <span>🕒 ${t.startTime || '--:--'} - ${t.endTime || '--:--'} (${t.estimatedDuration || 0} mins)</span>
                                            ${t.notes ? `<span>📝 ${t.notes}</span>` : ''}
                                        </div>
                                    </div>
                                </div>
                                <div class="task-actions">
                                    <button class="btn btn-sm btn-outline-primary" onclick="App.startPomodoroForTask('${t.id}')">Focus</button>
                                    <button class="btn btn-sm btn-secondary" onclick="App.openEditTaskModal('${t.id}')">Edit</button>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `}
            </div>
        `;
        container.innerHTML = html;
    },

    handleDayClick(dateStr) {
        // Show modal with tasks or option to add task for that day
        const tasks = this.tasksMap.get(dateStr) || [];
        const taskListHtml = tasks.length > 0 ? 
            tasks.map(t => `<li><strong>${t.startTime || 'Anytime'}:</strong> ${t.title} (${t.priority} Priority)</li>`).join('') :
            '<li>No tasks scheduled yet.</li>';

        App.openCreateTaskModal(dateStr);
    }
};
