// StudySync My Learning & Resume Where You Left Off Engine
const Learning = {
    paths: [],
    selectedPathId: null,

    async init() {
        await this.loadPaths();
    },

    async loadPaths() {
        try {
            this.paths = await API.getLearningPaths();
            if (this.paths.length > 0 && !this.selectedPathId) {
                this.selectedPathId = this.paths[0].id;
            }
            this.renderPathTabs();
            this.renderSelectedPath();
        } catch (e) {
            console.error('Failed to load learning paths:', e);
        }
    },

    selectPath(id) {
        this.selectedPathId = id;
        this.renderPathTabs();
        this.renderSelectedPath();
    },

    renderPathTabs() {
        const container = document.getElementById('learning-path-cards-container');
        if (!container) return;

        container.innerHTML = this.paths.map(p => {
            const isSelected = p.id === this.selectedPathId;
            return `
                <div class="learning-card ${isSelected ? 'selected' : ''}" onclick="Learning.selectPath('${p.id}')">
                    <div class="learning-card-top">
                        <div class="learning-card-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>
                        </div>
                        <span class="badge badge-cat">${p.category || 'Course'}</span>
                    </div>
                    <h3 style="font-size:1.1rem; font-weight:700; margin-bottom:0.35rem; color:var(--text-primary);">${p.title}</h3>
                    <p style="font-size:0.825rem; color:var(--text-muted); margin-bottom:1rem; flex:1;">${p.description || ''}</p>
                    <div>
                        <div style="display:flex; justify-content:space-between; font-size:0.8rem; font-weight:600; color:var(--text-secondary);">
                            <span>Progress</span>
                            <span>${p.progressPercentage || 0}% (${p.completedTopics || 0}/${p.totalTopics || 0})</span>
                        </div>
                        <div class="progress-bar-container">
                            <div class="progress-bar-fill" style="width: ${p.progressPercentage || 0}%;"></div>
                        </div>
                    </div>
                </div>
            `;
        }).join('') + `
            <div class="learning-card" style="border-style:dashed; align-items:center; justify-content:center; text-align:center; min-height:180px;" onclick="Learning.openNewPathModal()">
                <div style="width:40px; height:40px; border-radius:50%; background:var(--primary-light); color:var(--primary); display:flex; align-items:center; justify-content:center; margin-bottom:0.75rem;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                </div>
                <h4 style="font-size:1rem; font-weight:700; color:var(--text-primary); margin-bottom:0.25rem;">Create Learning Path</h4>
                <p style="font-size:0.8rem; color:var(--text-muted);">Add custom curriculum & topics</p>
            </div>
        `;
    },

    renderSelectedPath() {
        const path = this.paths.find(p => p.id === this.selectedPathId);
        const container = document.getElementById('selected-path-details-container');
        if (!container || !path) return;

        const lastStudiedDateFormatted = path.lastStudiedAt ? 
            new Date(path.lastStudiedAt).toLocaleDateString('default', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'Never';

        let html = `
            <!-- Resume Where You Left Off Hero -->
            <div class="resume-hero">
                <div class="resume-content">
                    <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.5rem;">
                        <span style="background:rgba(255,255,255,0.2); padding:0.2rem 0.6rem; border-radius:var(--radius-full); font-size:0.75rem; font-weight:700; letter-spacing:0.05em; text-transform:uppercase;">Resume Where You Left Off</span>
                    </div>
                    <h3>${path.title}</h3>
                    <div class="resume-meta">
                        <div class="resume-meta-item">
                            <span>Last Studied Topic</span>
                            <span>${path.lastStudiedTopicTitle || 'None yet'}</span>
                        </div>
                        <div class="resume-meta-item">
                            <span>Last Studied Date</span>
                            <span>${lastStudiedDateFormatted}</span>
                        </div>
                        <div class="resume-meta-item">
                            <span>Current Focus</span>
                            <span>${path.currentTopicTitle || 'Not started'}</span>
                        </div>
                        <div class="resume-meta-item">
                            <span>Next Topic Up</span>
                            <span>${path.nextTopicTitle || 'Ready to choose'}</span>
                        </div>
                    </div>
                    ${path.notes ? `
                        <div style="margin-top: 1rem; padding: 0.75rem 1rem; background: rgba(0,0,0,0.15); border-radius: var(--radius-md); font-size: 0.85rem;">
                            <strong>Latest Saved Notes:</strong> ${path.notes}
                        </div>
                    ` : ''}
                </div>
                <div>
                    <button class="btn btn-secondary" style="font-size:1rem; padding:0.85rem 1.5rem; background:white; color:var(--primary-text); font-weight:700; box-shadow:0 4px 14px rgba(0,0,0,0.15);" onclick="Learning.continueLearning('${path.id}')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
                        Continue Learning
                    </button>
                </div>
            </div>

            <!-- Path Curriculum Hierarchy -->
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.25rem;">
                <h3 style="font-size:1.25rem; font-weight:700; color:var(--text-primary);">Curriculum Structure</h3>
                <button class="btn btn-sm btn-outline-primary" onclick="Learning.openAddTopicModal('${path.id}')">+ Add Topic</button>
            </div>

            <div style="display:flex; flex-direction:column; gap:1.25rem;">
                ${(!path.subjects || path.subjects.length === 0) ? `
                    <div class="empty-state">
                        <h4>No subjects added yet</h4>
                        <p>Add subjects and chapters to structure this learning path.</p>
                    </div>
                ` : path.subjects.map(subject => `
                    <div class="card">
                        <div class="card-header" style="background:var(--bg-card-subtle);">
                            <div>
                                <h4 style="font-size:1.05rem; font-weight:700; color:var(--text-primary);">${subject.title}</h4>
                                <span style="font-size:0.8rem; color:var(--text-muted);">${subject.description || ''}</span>
                            </div>
                        </div>
                        <div class="card-body" style="padding:1.25rem; display:flex; flex-direction:column; gap:1rem;">
                            ${(!subject.chapters || subject.chapters.length === 0) ? `
                                <p style="font-size:0.85rem; color:var(--text-muted);">No chapters in this subject.</p>
                            ` : subject.chapters.map(chapter => `
                                <div style="border-left:3px solid var(--primary); padding-left:1rem; margin-bottom:0.75rem;">
                                    <h5 style="font-size:0.95rem; font-weight:700; margin-bottom:0.6rem; color:var(--text-primary);">${chapter.title}</h5>
                                    <div class="task-list">
                                        ${(!chapter.topics || chapter.topics.length === 0) ? `
                                            <p style="font-size:0.825rem; color:var(--text-muted);">No topics added yet.</p>
                                        ` : chapter.topics.map(topic => {
                                            const isDone = topic.status === 'Completed';
                                            const isInProgress = topic.status === 'In Progress';
                                            const statusClass = isDone ? 'badge-priority-low' : (isInProgress ? 'badge-priority-medium' : 'badge-cat');
                                            return `
                                                <div class="task-card ${isDone ? 'completed' : ''}" style="padding:0.85rem 1rem;">
                                                    <div class="task-left">
                                                        <div class="task-checkbox-custom" onclick="Learning.promptTopicCompletion('${path.id}', '${topic.id}', '${topic.title.replace(/'/g, "\\'")}', '${isDone ? 'Pending' : 'Completed'}')">
                                                            ${isDone ? '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg>' : ''}
                                                        </div>
                                                        <div class="task-main-info">
                                                            <div style="display:flex; align-items:center; gap:0.5rem; flex-wrap:wrap;">
                                                                <span class="task-title" style="font-size:0.925rem;">${topic.title}</span>
                                                                <span class="badge ${statusClass}">${topic.status || 'Pending'}</span>
                                                                ${topic.studyMinutes ? `<span style="font-size:0.75rem; color:var(--text-muted);">⏳ ${topic.studyMinutes}m studied</span>` : ''}
                                                            </div>
                                                            <p style="font-size:0.8rem; color:var(--text-muted); margin:0.2rem 0 0 0;">${topic.description || ''}</p>
                                                            ${topic.notes ? `<p style="font-size:0.78rem; color:var(--text-secondary); margin:0.2rem 0 0 0; background:var(--bg-card-subtle); padding:0.25rem 0.5rem; border-radius:4px;">📝 ${topic.notes}</p>` : ''}
                                                        </div>
                                                    </div>
                                                    <div class="task-actions">
                                                        <button class="btn btn-sm btn-outline-primary" onclick="Learning.startFocusOnTopic('${topic.id}', '${topic.title.replace(/'/g, "\\'")}', '${path.category || 'Learning'}')">Focus</button>
                                                        <button class="btn btn-sm btn-secondary" onclick="Learning.openEditNotesModal('${path.id}', '${topic.id}', '${topic.title.replace(/'/g, "\\'")}', '${(topic.notes || '').replace(/'/g, "\\'")}')">Notes</button>
                                                    </div>
                                                </div>
                                            `;
                                        }).join('')}
                                    </div>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;

        container.innerHTML = html;
    },

    continueLearning(pathId) {
        const path = this.paths.find(p => p.id === pathId);
        if (!path) return;

        const topicTitle = path.currentTopicTitle || path.nextTopicTitle || path.title;
        const topicId = path.currentTopicId || path.nextTopicId;

        if (topicId) {
            this.startFocusOnTopic(topicId, topicTitle, path.category || 'Learning');
        } else {
            Notifications.toast(`Continuing ${path.title}. Select a topic to focus!`, 'info');
        }
    },

    startFocusOnTopic(topicId, topicTitle, category) {
        Pomodoro.selectTaskDirectly(null, topicTitle, category);
        App.switchView('pomodoro');
        Notifications.toast(`Pomodoro focus session ready for "${topicTitle}".`, 'success');
    },

    async promptTopicCompletion(pathId, topicId, topicTitle, newStatus) {
        if (newStatus === 'Completed') {
            // Confirm and offer Spaced Repetition scheduling
            const modal = document.getElementById('modal-topic-complete');
            if (modal) {
                document.getElementById('topic-complete-title').textContent = topicTitle;
                document.getElementById('btn-confirm-complete-topic').onclick = async () => {
                    const scheduleRev = document.getElementById('chk-schedule-spaced-rev').checked;
                    await Learning.markTopicCompleteWithRevision(pathId, topicId, topicTitle, scheduleRev);
                    App.closeModal('modal-topic-complete');
                };
                App.openModal('modal-topic-complete');
            } else {
                await this.markTopicCompleteWithRevision(pathId, topicId, topicTitle, true);
            }
        } else {
            // Reset to pending
            await API.updateTopicStatus(pathId, topicId, 'Pending');
            await this.loadPaths();
            Notifications.toast(`Topic marked as pending.`, 'info');
        }
    },

    async markTopicCompleteWithRevision(pathId, topicId, topicTitle, scheduleRevisions) {
        try {
            const path = this.paths.find(p => p.id === pathId);
            await API.updateTopicStatus(pathId, topicId, 'Completed');

            if (scheduleRevisions) {
                // Fetch settings for custom revision intervals
                const settings = await API.getSettings();
                const intervals = settings.revisionIntervals || [1, 3, 7, 15];
                await API.scheduleTopicRevisions(pathId, path.title, topicId, topicTitle, intervals, true);
                Notifications.toast(`Topic completed! Spaced revisions (+1d, +3d, +7d, +15d) scheduled on your calendar.`, 'success');
            } else {
                Notifications.toast(`Topic completed! Course progress updated.`, 'success');
            }

            await this.loadPaths();
            App.refreshDashboard();
        } catch (e) {
            console.error('Failed to update topic completion:', e);
            Notifications.toast('Error updating topic: ' + e.message, 'error');
        }
    },

    openEditNotesModal(pathId, topicId, topicTitle, currentNotes) {
        const modal = document.getElementById('modal-topic-notes');
        if (!modal) return;

        document.getElementById('notes-modal-topic-title').textContent = topicTitle;
        document.getElementById('topic-notes-textarea').value = currentNotes || '';
        
        document.getElementById('btn-save-topic-notes').onclick = async () => {
            const notes = document.getElementById('topic-notes-textarea').value;
            try {
                await API.updateTopicStatus(pathId, topicId, 'In Progress', notes);
                Notifications.toast('Notes saved successfully!', 'success');
                App.closeModal('modal-topic-notes');
                await this.loadPaths();
            } catch (e) {
                Notifications.toast('Failed to save notes: ' + e.message, 'error');
            }
        };

        App.openModal('modal-topic-notes');
    },

    openNewPathModal() {
        App.openModal('modal-create-path');
    },

    async saveNewLearningPath(event) {
        event.preventDefault();
        const title = document.getElementById('new-path-title').value;
        const category = document.getElementById('new-path-category').value;
        const description = document.getElementById('new-path-description').value;

        if (!title) {
            Notifications.toast('Please provide a course title.', 'error');
            return;
        }

        try {
            const newPath = await API.createLearningPath({
                title,
                category,
                description,
                icon: 'book',
                color: 'emerald',
                subjects: []
            });
            Notifications.toast(`Learning path "${title}" created!`, 'success');
            App.closeModal('modal-create-path');
            await this.loadPaths();
            this.selectPath(newPath.id);
        } catch (e) {
            Notifications.toast('Failed to create learning path: ' + e.message, 'error');
        }
    },

    openAddTopicModal(pathId) {
        const path = this.paths.find(p => p.id === pathId);
        if (!path) return;

        if (!path.subjects || path.subjects.length === 0) {
            Notifications.toast('Please create at least one subject in this path first.', 'info');
            return;
        }

        // populate subject & chapter dropdowns in modal
        const subjectSelect = document.getElementById('topic-modal-subject-select');
        const chapterSelect = document.getElementById('topic-modal-chapter-select');
        if (!subjectSelect || !chapterSelect) return;

        subjectSelect.innerHTML = path.subjects.map(s => `<option value="${s.id}">${s.title}</option>`).join('');
        
        const updateChapters = () => {
            const selSub = path.subjects.find(s => s.id === subjectSelect.value);
            if (selSub && selSub.chapters) {
                chapterSelect.innerHTML = selSub.chapters.map(c => `<option value="${c.id}">${c.title}</option>`).join('');
            } else {
                chapterSelect.innerHTML = '<option value="">No chapters</option>';
            }
        };

        subjectSelect.onchange = updateChapters;
        updateChapters();

        document.getElementById('btn-save-new-topic').onclick = async () => {
            const subjectId = subjectSelect.value;
            const chapterId = chapterSelect.value;
            const title = document.getElementById('topic-modal-title').value;
            const description = document.getElementById('topic-modal-desc').value;

            if (!title) {
                Notifications.toast('Please enter topic title', 'error');
                return;
            }

            try {
                await API.addTopic(pathId, subjectId, chapterId, {
                    title,
                    description,
                    status: 'Pending'
                });
                Notifications.toast(`Topic "${title}" added to curriculum!`, 'success');
                App.closeModal('modal-add-topic');
                await this.loadPaths();
            } catch (e) {
                Notifications.toast('Failed to add topic: ' + e.message, 'error');
            }
        };

        App.openModal('modal-add-topic');
    }
};
