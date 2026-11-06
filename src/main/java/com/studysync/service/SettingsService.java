package com.studysync.service;

import com.studysync.model.UserSettings;
import com.studysync.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final UserSettingsRepository settingsRepository;

    public SettingsService(UserSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public UserSettings getSettings(String userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> settingsRepository.save(new UserSettings(userId)));
    }

    public UserSettings updateSettings(String userId, UserSettings updated) {
        UserSettings current = getSettings(userId);
        current.setDailyStudyHours(updated.getDailyStudyHours());
        current.setPreferredStudyTime(updated.getPreferredStudyTime());
        if (updated.getRevisionIntervals() != null && !updated.getRevisionIntervals().isEmpty()) {
            current.setRevisionIntervals(updated.getRevisionIntervals());
        }
        current.setNotificationSound(updated.isNotificationSound());
        current.setBrowserNotificationsEnabled(updated.isBrowserNotificationsEnabled());
        current.setReminderMinutesBefore(updated.getReminderMinutesBefore());
        current.setThemeMode(updated.getThemeMode());
        current.setWeekStartDay(updated.getWeekStartDay());
        current.setFocusDuration(updated.getFocusDuration());
        current.setShortBreakDuration(updated.getShortBreakDuration());
        current.setLongBreakDuration(updated.getLongBreakDuration());
        return settingsRepository.save(current);
    }
}

