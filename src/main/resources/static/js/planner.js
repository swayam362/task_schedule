// StudySync Smart Daily Planner Engine
const Planner = {
    currentSuggestions: null,

    init() {
        const dateInput = document.getElementById('planner-target-date');
        if (dateInput) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }
    },

    async generatePlan() {
        const dateInput = document.getElementById('planner-target-date');
        const targetDate = dateInput ? dateInput.value : new Date().toISOString().split('T')[0];
        const container = document.getElementById('planner-results-container');
        if (!container) return;

        container.innerHTML = `
            <div style="padding: 2.5rem; text-align: center; color: var(--text-muted);">
                <div class="spinner" style="margin: 0 auto 1rem; width: 32px; height: 32px; border: 3px solid var(--border-color); border-top-color: var(--primary); border-radius: 50%; animation: spin 1s linear infinite;"></div>
                <p>Analyzing pending revisions, upcoming exams, missed tasks and available study hours...</p>
            </div>
            <style>@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }</style>
        `;

        try {
            const data = await API.getPlannerSuggestions(targetDate);
            this.currentSuggestions = data;
            this.renderSuggestions(data);
        } catch (e) {
            console.error('Failed to generate daily plan:', e);
            container.innerHTML = `
                <div class="empty-state">
                    <h4>Could not generate plan</h4>
                    <p>${e.message || 'Please check your connection and try again.'}</p>
                </div>
            `;
        }
    },

    renderSuggestions(data) {
        const container = document.getElementById('planner-results-container');
        if (!container) return;

        if (!data.slots || data.slots.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <h4>No specific items needed today!</h4>
                    <p>Your workload is clear. You can add tasks manually or take a well-deserved rest.</p>
                </div>
            `;
            return;
        }

        let html = `
            <div style="background: var(--bg-card-subtle); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 1.25rem; margin-bottom: 1.5rem;">
                <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.5rem; margin-bottom:0.5rem;">
                    <span style="font-weight:700; color:var(--primary); font-size:0.9rem;">Rule-Based Strategic Plan</span>
                    <span style="font-size:0.8rem; color:var(--text-muted);">Allocating ~${data.totalAvailableHours} available hours</span>
                </div>
                <p style="font-size:0.875rem; color:var(--text-secondary); margin:0;">${data.strategySummary}</p>
            </div>

            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
                <h4 style="font-size:1.05rem; font-weight:700;">Proposed Schedule Slots (${data.slots.length})</h4>
                <div style="display:flex; gap:0.5rem;">
                    <button class="btn btn-sm btn-secondary" onclick="Planner.selectAll(true)">Select All</button>
                    <button class="btn btn-sm btn-secondary" onclick="Planner.selectAll(false)">Deselect All</button>
                </div>
            </div>

            <div class="task-list" style="margin-bottom: 1.5rem;">
                ${data.slots.map((slot, idx) => {
                    const pClass = slot.priority === 'High' ? 'badge-priority-high' : (slot.priority === 'Medium' ? 'badge-priority-medium' : 'badge-priority-low');
                    return `
                        <div class="task-card" style="align-items:flex-start;">
                            <div style="display:flex; align-items:flex-start; gap:1rem; flex:1;">
                                <input type="checkbox" class="planner-slot-checkbox" data-index="${idx}" checked style="width:20px; height:20px; margin-top:2px; accent-color:var(--primary); cursor:pointer;">
                                <div class="task-main-info" style="flex:1;">
                                    <div style="display:flex; align-items:center; gap:0.5rem; flex-wrap:wrap;">
                                        <span class="task-title">${slot.title}</span>
                                        <span class="badge ${pClass}">${slot.priority}</span>
                                        <span class="badge badge-cat">${slot.category}</span>
                                        <span style="font-size:0.75rem; color:var(--primary); font-weight:600;">🕒 ${slot.startTime} – ${slot.endTime} (${slot.durationMinutes}m)</span>
                                    </div>
                                    <p style="font-size:0.825rem; color:var(--text-muted); margin:0.35rem 0 0 0;">💡 <em>${slot.reason}</em></p>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.75rem;">
                <button class="btn btn-primary" onclick="Planner.applySelectedSlots()">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>
                    Apply Selected Slots to My Schedule
                </button>
            </div>
        `;

        container.innerHTML = html;
    },

    selectAll(check) {
        document.querySelectorAll('.planner-slot-checkbox').forEach(cb => cb.checked = check);
    },

    async applySelectedSlots() {
        if (!this.currentSuggestions || !this.currentSuggestions.slots) return;

        const selectedSlots = [];
        document.querySelectorAll('.planner-slot-checkbox').forEach(cb => {
            if (cb.checked) {
                const idx = parseInt(cb.dataset.index, 10);
                selectedSlots.push(this.currentSuggestions.slots[idx]);
            }
        });

        if (selectedSlots.length === 0) {
            Notifications.toast('Please select at least one schedule slot to apply.', 'info');
            return;
        }

        const dateInput = document.getElementById('planner-target-date');
        const targetDate = dateInput ? dateInput.value : new Date().toISOString().split('T')[0];

        try {
            await API.applyPlan(targetDate, selectedSlots);
            Notifications.toast(`Applied ${selectedSlots.length} tasks to your schedule for ${targetDate}!`, 'success');
            
            // Refresh dashboard and timetable
            if (window.App) {
                App.loadTasks();
                App.refreshDashboard();
                App.switchView('schedule');
            }
        } catch (e) {
            console.error('Failed to apply planner slots:', e);
            Notifications.toast('Failed to apply schedule: ' + e.message, 'error');
        }
    }
};
