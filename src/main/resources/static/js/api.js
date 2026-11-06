// StudySync REST API Client
const API_BASE = '/api';
const CURRENT_USER_ID = 'user-sainath';

const API = {
    // Dashboard
    async getDashboardStats() {
        const res = await fetch(`${API_BASE}/dashboard/stats?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch dashboard stats');
        return await res.json();
    },

    // Tasks
    async getTasks(filters = {}) {
        const params = new URLSearchParams({ userId: CURRENT_USER_ID, ...filters });
        const res = await fetch(`${API_BASE}/tasks?${params}`);
        if (!res.ok) throw new Error('Failed to fetch tasks');
        return await res.json();
    },

    async getTaskById(id) {
        const res = await fetch(`${API_BASE}/tasks/${id}`);
        if (!res.ok) throw new Error('Failed to fetch task');
        return await res.json();
    },

    async checkConflict(task) {
        const res = await fetch(`${API_BASE}/tasks/check-conflict`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...task, userId: CURRENT_USER_ID })
        });
        if (!res.ok) throw new Error('Failed to check conflict');
        return await res.json();
    },

    async createTask(task, forceOverride = false) {
        const res = await fetch(`${API_BASE}/tasks?forceOverride=${forceOverride}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...task, userId: CURRENT_USER_ID })
        });
        if (res.status === 409) {
            const err = await res.json();
            const e = new Error(err.message || 'Schedule Conflict Detected');
            e.isConflict = true;
            throw e;
        }
        if (!res.ok) throw new Error('Failed to create task');
        return await res.json();
    },

    async updateTask(id, task, forceOverride = false) {
        const res = await fetch(`${API_BASE}/tasks/${id}?forceOverride=${forceOverride}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...task, userId: CURRENT_USER_ID })
        });
        if (res.status === 409) {
            const err = await res.json();
            const e = new Error(err.message || 'Schedule Conflict Detected');
            e.isConflict = true;
            throw e;
        }
        if (!res.ok) throw new Error('Failed to update task');
        return await res.json();
    },

    async deleteTask(id) {
        const res = await fetch(`${API_BASE}/tasks/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete task');
        return true;
    },

    async markTaskCompleted(id) {
        const res = await fetch(`${API_BASE}/tasks/${id}/complete`, { method: 'PATCH' });
        if (!res.ok) throw new Error('Failed to mark task completed');
        return await res.json();
    },

    async changeTaskStatus(id, status) {
        const res = await fetch(`${API_BASE}/tasks/${id}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if (!res.ok) throw new Error('Failed to change status');
        return await res.json();
    },

    async rescheduleTask(id, date, startTime, endTime) {
        const res = await fetch(`${API_BASE}/tasks/${id}/reschedule`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ date, startTime, endTime })
        });
        if (!res.ok) throw new Error('Failed to reschedule task');
        return await res.json();
    },

    async getMissedTasks() {
        const res = await fetch(`${API_BASE}/tasks/missed?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch missed tasks');
        return await res.json();
    },

    // Learning Paths
    async getLearningPaths() {
        const res = await fetch(`${API_BASE}/learning-paths?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch learning paths');
        return await res.json();
    },

    async getLearningPathById(id) {
        const res = await fetch(`${API_BASE}/learning-paths/${id}`);
        if (!res.ok) throw new Error('Failed to fetch learning path');
        return await res.json();
    },

    async createLearningPath(path) {
        const res = await fetch(`${API_BASE}/learning-paths`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...path, userId: CURRENT_USER_ID })
        });
        if (!res.ok) throw new Error('Failed to create learning path');
        return await res.json();
    },

    async updateLearningPath(id, path) {
        const res = await fetch(`${API_BASE}/learning-paths/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(path)
        });
        if (!res.ok) throw new Error('Failed to update learning path');
        return await res.json();
    },

    async deleteLearningPath(id) {
        const res = await fetch(`${API_BASE}/learning-paths/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete learning path');
        return true;
    },

    async updateTopicStatus(pathId, topicId, status, notes = null, studyMinutes = null) {
        const res = await fetch(`${API_BASE}/learning-paths/${pathId}/topics/${topicId}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status, notes, studyMinutes })
        });
        if (!res.ok) throw new Error('Failed to update topic status');
        return await res.json();
    },

    async addTopic(pathId, subjectId, chapterId, topic) {
        const res = await fetch(`${API_BASE}/learning-paths/${pathId}/subjects/${subjectId}/chapters/${chapterId}/topics`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(topic)
        });
        if (!res.ok) throw new Error('Failed to add topic');
        return await res.json();
    },

    // Revisions
    async getRevisions(filter = 'all') {
        const res = await fetch(`${API_BASE}/revisions?userId=${CURRENT_USER_ID}&filter=${filter}`);
        if (!res.ok) throw new Error('Failed to fetch revisions');
        return await res.json();
    },

    async createRevision(revision) {
        const res = await fetch(`${API_BASE}/revisions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...revision, userId: CURRENT_USER_ID })
        });
        if (!res.ok) throw new Error('Failed to create revision');
        return await res.json();
    },

    async scheduleTopicRevisions(pathId, pathTitle, topicId, topicTitle, intervals, createTasks = true) {
        const res = await fetch(`${API_BASE}/revisions/schedule-topic`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: CURRENT_USER_ID,
                pathId,
                pathTitle,
                topicId,
                topicTitle,
                intervals,
                createTasks
            })
        });
        if (!res.ok) throw new Error('Failed to schedule revisions');
        return await res.json();
    },

    async markRevisionCompleted(id) {
        const res = await fetch(`${API_BASE}/revisions/${id}/complete`, { method: 'PATCH' });
        if (!res.ok) throw new Error('Failed to complete revision');
        return await res.json();
    },

    async rescheduleRevision(id, date) {
        const res = await fetch(`${API_BASE}/revisions/${id}/reschedule`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ date })
        });
        if (!res.ok) throw new Error('Failed to reschedule revision');
        return await res.json();
    },

    async deleteRevision(id) {
        const res = await fetch(`${API_BASE}/revisions/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete revision');
        return true;
    },

    // Exams & Assignments
    async getExams() {
        const res = await fetch(`${API_BASE}/academic/exams?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch exams');
        return await res.json();
    },

    async createExam(exam) {
        const res = await fetch(`${API_BASE}/academic/exams`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...exam, userId: CURRENT_USER_ID })
        });
        if (!res.ok) throw new Error('Failed to create exam');
        return await res.json();
    },

    async updateExam(id, exam) {
        const res = await fetch(`${API_BASE}/academic/exams/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(exam)
        });
        if (!res.ok) throw new Error('Failed to update exam');
        return await res.json();
    },

    async deleteExam(id) {
        const res = await fetch(`${API_BASE}/academic/exams/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete exam');
        return true;
    },

    async generateExamTasks(examId, daysBefore = 3, hoursPerDay = 2) {
        const res = await fetch(`${API_BASE}/academic/exams/${examId}/generate-tasks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ daysBefore, hoursPerDay })
        });
        if (!res.ok) throw new Error('Failed to generate exam tasks');
        return await res.json();
    },

    async getAssignments() {
        const res = await fetch(`${API_BASE}/academic/assignments?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch assignments');
        return await res.json();
    },

    async createAssignment(assignment) {
        const res = await fetch(`${API_BASE}/academic/assignments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...assignment, userId: CURRENT_USER_ID })
        });
        if (!res.ok) throw new Error('Failed to create assignment');
        return await res.json();
    },

    async updateAssignment(id, assignment) {
        const res = await fetch(`${API_BASE}/academic/assignments/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(assignment)
        });
        if (!res.ok) throw new Error('Failed to update assignment');
        return await res.json();
    },

    async deleteAssignment(id) {
        const res = await fetch(`${API_BASE}/academic/assignments/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete assignment');
        return true;
    },

    async generateAssignmentTask(assignmentId) {
        const res = await fetch(`${API_BASE}/academic/assignments/${assignmentId}/generate-task`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error('Failed to generate assignment task');
        return await res.json();
    },

    // Focus Sessions
    async recordFocusSession(session) {
        const res = await fetch(`${API_BASE}/focus-sessions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...session, userId: CURRENT_USER_ID })
        });
        if (!res.ok) throw new Error('Failed to record focus session');
        return await res.json();
    },

    // Analytics
    async getAnalytics() {
        const res = await fetch(`${API_BASE}/analytics?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch analytics');
        return await res.json();
    },

    // Planner
    async getPlannerSuggestions(date) {
        const params = new URLSearchParams({ userId: CURRENT_USER_ID });
        if (date) params.append('date', date);
        const res = await fetch(`${API_BASE}/planner/suggest?${params}`);
        if (!res.ok) throw new Error('Failed to fetch planner suggestions');
        return await res.json();
    },

    async applyPlan(date, slots) {
        const res = await fetch(`${API_BASE}/planner/apply?userId=${CURRENT_USER_ID}&date=${date}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(slots)
        });
        if (!res.ok) throw new Error('Failed to apply plan');
        return await res.json();
    },

    // Settings
    async getSettings() {
        const res = await fetch(`${API_BASE}/settings?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch settings');
        return await res.json();
    },

    async updateSettings(settings) {
        const res = await fetch(`${API_BASE}/settings?userId=${CURRENT_USER_ID}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(settings)
        });
        if (!res.ok) throw new Error('Failed to update settings');
        return await res.json();
    },

    // AI Study Advisor
    async getAIAnalysis() {
        const res = await fetch(`${API_BASE}/ai/analysis?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to fetch AI study analysis');
        return await res.json();
    },

    async askAIAdvisor(prompt) {
        const res = await fetch(`${API_BASE}/ai/ask?userId=${CURRENT_USER_ID}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt })
        });
        if (!res.ok) throw new Error('Failed to consult AI advisor');
        return await res.json();
    },

    // Data Export
    async exportData() {
        const res = await fetch(`${API_BASE}/data/export?userId=${CURRENT_USER_ID}`);
        if (!res.ok) throw new Error('Failed to export data');
        return await res.json();
    }
};

