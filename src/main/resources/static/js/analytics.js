// StudySync Productivity Analytics Engine (Chart.js)
const Analytics = {
    dailyChart: null,
    weeklyChart: null,
    subjectChart: null,
    statusChart: null,

    async init() {
        await this.loadAnalytics();
    },

    async loadAnalytics() {
        try {
            const data = await API.getAnalytics();
            this.renderSummaryCards(data);
            this.renderCharts(data);
        } catch (e) {
            console.error('Failed to load analytics:', e);
        }
    },

    renderSummaryCards(data) {
        const streakEl = document.getElementById('analytics-streak-val');
        if (streakEl) streakEl.textContent = `${data.currentStreak} Days 🔥`;

        const totalHoursEl = document.getElementById('analytics-hours-val');
        if (totalHoursEl) totalHoursEl.textContent = `${data.totalStudyHours} hrs`;

        const weeklyRateEl = document.getElementById('analytics-weekly-val');
        if (weeklyRateEl) weeklyRateEl.textContent = `${data.weeklyCompletionRate}%`;

        const revRateEl = document.getElementById('analytics-revision-val');
        if (revRateEl) revRateEl.textContent = `${data.revisionConsistencyRate}%`;

        const sessionsEl = document.getElementById('analytics-sessions-val');
        if (sessionsEl) sessionsEl.textContent = `${data.totalSessions} Sessions`;
    },

    renderCharts(data) {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        const textColor = isDark ? '#cbd5e1' : '#475569';
        const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)';

        // 1. Daily Completed vs Created Tasks
        const ctxDaily = document.getElementById('chart-daily-tasks');
        if (ctxDaily && window.Chart) {
            if (this.dailyChart) this.dailyChart.destroy();
            this.dailyChart = new Chart(ctxDaily, {
                type: 'bar',
                data: {
                    labels: data.dailyLabels || [],
                    datasets: [
                        {
                            label: 'Completed Tasks',
                            data: data.dailyCompletedData || [],
                            backgroundColor: '#10b981',
                            borderRadius: 6
                        },
                        {
                            label: 'Created Tasks',
                            data: data.dailyCreatedData || [],
                            backgroundColor: isDark ? '#334155' : '#e2e8f0',
                            borderRadius: 6
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { labels: { color: textColor } }
                    },
                    scales: {
                        x: { grid: { color: gridColor }, ticks: { color: textColor } },
                        y: { beginAtZero: true, grid: { color: gridColor }, ticks: { color: textColor, precision: 0 } }
                    }
                }
            });
        }

        // 2. Weekly Productivity Trend
        const ctxWeekly = document.getElementById('chart-weekly-trend');
        if (ctxWeekly && window.Chart) {
            if (this.weeklyChart) this.weeklyChart.destroy();
            this.weeklyChart = new Chart(ctxWeekly, {
                type: 'line',
                data: {
                    labels: data.weeklyTrendLabels || ['W-3', 'W-2', 'W-1', 'Current'],
                    datasets: [{
                        label: 'Completion Rate (%)',
                        data: data.weeklyTrendData || [60, 75, 80, 85],
                        borderColor: '#059669',
                        backgroundColor: 'rgba(16, 185, 129, 0.15)',
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: '#10b981',
                        pointRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { labels: { color: textColor } }
                    },
                    scales: {
                        x: { grid: { color: gridColor }, ticks: { color: textColor } },
                        y: { beginAtZero: true, max: 100, grid: { color: gridColor }, ticks: { color: textColor } }
                    }
                }
            });
        }

        // 3. Subject-wise Study Hours Breakdown
        const ctxSubject = document.getElementById('chart-subject-hours');
        if (ctxSubject && window.Chart) {
            if (this.subjectChart) this.subjectChart.destroy();
            const labels = Object.keys(data.subjectStudyMinutes || {});
            const values = Object.values(data.subjectStudyMinutes || {}).map(m => Math.round((m / 60) * 10) / 10);

            this.subjectChart = new Chart(ctxSubject, {
                type: 'doughnut',
                data: {
                    labels: labels.length ? labels : ['DSA', 'Java Backend', 'Python', 'SQL'],
                    datasets: [{
                        data: values.length ? values : [4, 6, 3, 2.5],
                        backgroundColor: [
                            '#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ec4899', '#14b8a6', '#64748b'
                        ],
                        borderWidth: 2,
                        borderColor: isDark ? '#1e293b' : '#ffffff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { color: textColor, boxWidth: 12 } },
                        tooltip: {
                            callbacks: {
                                label: (ctx) => `${ctx.label}: ${ctx.raw} study hours`
                            }
                        }
                    }
                }
            });
        }

        // 4. Task Status Breakdown
        const ctxStatus = document.getElementById('chart-task-status');
        if (ctxStatus && window.Chart) {
            if (this.statusChart) this.statusChart.destroy();
            const counts = data.taskStatusCounts || {};

            this.statusChart = new Chart(ctxStatus, {
                type: 'pie',
                data: {
                    labels: ['Completed', 'Pending', 'In Progress', 'Missed'],
                    datasets: [{
                        data: [
                            counts['Completed'] || 0,
                            counts['Pending'] || 0,
                            counts['In Progress'] || 0,
                            counts['Missed'] || 0
                        ],
                        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444'],
                        borderWidth: 2,
                        borderColor: isDark ? '#1e293b' : '#ffffff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { color: textColor, boxWidth: 12 } }
                    }
                }
            });
        }
    }
};

