// StudySync Notification & Reminder Engine
const Notifications = {
    notificationsList: [],
    notifiedTaskIds: new Set(),
    audioCtx: null,

    init() {
        this.renderNotificationCenter();
        this.checkBrowserPermission();
        this.startReminderLoop();
    },

    playChime() {
        try {
            if (!this.audioCtx) {
                this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            }
            if (this.audioCtx.state === 'suspended') {
                this.audioCtx.resume();
            }
            const osc = this.audioCtx.createOscillator();
            const gain = this.audioCtx.createGain();

            osc.type = 'sine';
            osc.frequency.setValueAtTime(587.33, this.audioCtx.currentTime); // D5
            osc.frequency.exponentialRampToValueAtTime(880, this.audioCtx.currentTime + 0.15); // A5

            gain.gain.setValueAtTime(0.15, this.audioCtx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.6);

            osc.connect(gain);
            gain.connect(this.audioCtx.destination);

            osc.start();
            osc.stop(this.audioCtx.currentTime + 0.6);
        } catch (e) {
            console.log('Audio playback note:', e);
        }
    },

    checkBrowserPermission() {
        if (!('Notification' in window)) {
            console.log('Browser does not support desktop notifications.');
            return;
        }
        if (Notification.permission === 'default') {
            // Provide gentle notification banner on UI
            const banner = document.getElementById('notif-permission-banner');
            if (banner) banner.style.display = 'flex';
        }
    },

    async requestPermission() {
        if (!('Notification' in window)) {
            this.toast('Notifications are not supported by this browser.', 'error');
            return;
        }
        const perm = await Notification.requestPermission();
        const banner = document.getElementById('notif-permission-banner');
        if (banner) banner.style.display = 'none';

        if (perm === 'granted') {
            this.toast('Browser notifications enabled successfully!', 'success');
            this.sendNativeNotification('StudySync Notifications Active', 'You will receive reminders when tasks and revisions are due.');
        } else {
            this.toast('Notifications blocked in browser settings.', 'info');
        }
    },

    sendNativeNotification(title, body, actions = []) {
        if ('Notification' in window && Notification.permission === 'granted') {
            try {
                const notif = new Notification(title, {
                    body: body,
                    icon: '/favicon.ico'
                });
                notif.onclick = () => {
                    window.focus();
                };
            } catch (e) {
                console.log('Native notification error:', e);
            }
        }
    },

    toast(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>';
        if (type === 'error') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';
        } else if (type === 'info') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
        }

        toast.innerHTML = `
            ${iconSvg}
            <div style="flex: 1; font-size: 0.875rem; font-weight: 500;">${message}</div>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.transition = 'all 0.3s ease';
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    addInAppNotification(title, message, time, type = 'task', actionData = null) {
        const item = {
            id: 'notif-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7),
            title,
            message,
            time,
            type,
            actionData,
            read: false
        };
        this.notificationsList.unshift(item);
        this.playChime();
        this.renderNotificationCenter();
    },

    renderNotificationCenter() {
        const badge = document.getElementById('notif-badge-count');
        const listEl = document.getElementById('notif-center-list');
        const unreadCount = this.notificationsList.filter(n => !n.read).length;

        if (badge) {
            if (unreadCount > 0) {
                badge.style.display = 'flex';
                badge.textContent = unreadCount > 9 ? '9+' : unreadCount;
            } else {
                badge.style.display = 'none';
            }
        }

        if (listEl) {
            if (this.notificationsList.length === 0) {
                listEl.innerHTML = `
                    <div style="padding: 2rem; text-align: center; color: var(--text-muted); font-size: 0.85rem;">
                        No new notifications. You are completely up to date!
                    </div>
                `;
            } else {
                listEl.innerHTML = this.notificationsList.map(n => `
                    <div style="padding: 0.85rem 1rem; border-bottom: 1px solid var(--border-color); display: flex; flex-direction: column; gap: 0.35rem; ${!n.read ? 'background: var(--primary-light);' : ''}">
                        <div style="display: flex; align-items: center; justify-content: space-between;">
                            <span style="font-weight: 600; font-size: 0.875rem; color: var(--text-primary);">${n.title}</span>
                            <span style="font-size: 0.72rem; color: var(--text-muted);">${n.time}</span>
                        </div>
                        <p style="font-size: 0.825rem; color: var(--text-secondary); margin: 0;">${n.message}</p>
                        ${n.actionData && n.actionData.taskId ? `
                            <div style="display: flex; gap: 0.5rem; margin-top: 0.35rem;">
                                <button class="btn btn-sm btn-primary" onclick="App.startPomodoroForTask('${n.actionData.taskId}'); Notifications.markRead('${n.id}');">Start Task</button>
                                <button class="btn btn-sm btn-secondary" onclick="App.completeTaskQuick('${n.actionData.taskId}'); Notifications.markRead('${n.id}');">Complete</button>
                            </div>
                        ` : ''}
                    </div>
                `).join('');
            }
        }
    },

    markRead(id) {
        const item = this.notificationsList.find(n => n.id === id);
        if (item) item.read = true;
        this.renderNotificationCenter();
    },

    markAllRead() {
        this.notificationsList.forEach(n => n.read = true);
        this.renderNotificationCenter();
    },

    startReminderLoop() {
        // Run check immediately and then every 30 seconds
        this.checkReminders();
        setInterval(() => this.checkReminders(), 30000);
    },

    async checkReminders() {
        try {
            const todayStr = new Date().toISOString().split('T')[0];
            const tasks = await API.getTasks({ date: todayStr });
            const now = new Date();
            const currentMinutes = now.getHours() * 60 + now.getMinutes();

            for (const task of tasks) {
                if (task.status === 'Completed' || this.notifiedTaskIds.has(task.id)) {
                    continue;
                }
                if (task.startTime) {
                    const [h, m] = task.startTime.split(':').map(Number);
                    const taskMinutes = h * 60 + m;
                    const leadMinutes = task.reminderMinutesBefore || 10;
                    
                    // Remind leadMinutes before or when task starts
                    const diff = taskMinutes - currentMinutes;
                    if (diff <= leadMinutes && diff >= 0) {
                        this.notifiedTaskIds.add(task.id);
                        const msg = diff === 0 ? `Task "${task.title}" is starting now!` : `Task "${task.title}" starts in ${diff} minutes.`;
                        
                        this.addInAppNotification(
                            diff === 0 ? 'Task Starting' : 'Upcoming Task Reminder',
                            msg,
                            task.startTime,
                            'task',
                            { taskId: task.id }
                        );
                        this.sendNativeNotification('StudySync Reminder', msg);
                    }
                }
            }
        } catch (e) {
            // fail gracefully
        }
    }
};
