package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "user_settings")
public class UserSettings {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private int dailyStudyHours = 6;
    private String preferredStudyTime = "Morning"; // Morning, Afternoon, Evening, Night
    private List<Integer> revisionIntervals = new ArrayList<>(List.of(1, 3, 7, 15));
    private boolean notificationSound = true;
    private boolean browserNotificationsEnabled = true;
    private int reminderMinutesBefore = 10;
    private String themeMode = "light"; // light, dark
    private String weekStartDay = "Monday"; // Monday, Sunday
    private int focusDuration = 25;
    private int shortBreakDuration = 5;
    private int longBreakDuration = 15;

    public UserSettings() {}

    public UserSettings(String userId) {
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getDailyStudyHours() { return dailyStudyHours; }
    public void setDailyStudyHours(int dailyStudyHours) { this.dailyStudyHours = dailyStudyHours; }

    public String getPreferredStudyTime() { return preferredStudyTime; }
    public void setPreferredStudyTime(String preferredStudyTime) { this.preferredStudyTime = preferredStudyTime; }

    public List<Integer> getRevisionIntervals() { return revisionIntervals; }
    public void setRevisionIntervals(List<Integer> revisionIntervals) { this.revisionIntervals = revisionIntervals; }

    public boolean isNotificationSound() { return notificationSound; }
    public void setNotificationSound(boolean notificationSound) { this.notificationSound = notificationSound; }

    public boolean isBrowserNotificationsEnabled() { return browserNotificationsEnabled; }
    public void setBrowserNotificationsEnabled(boolean browserNotificationsEnabled) { this.browserNotificationsEnabled = browserNotificationsEnabled; }

    public int getReminderMinutesBefore() { return reminderMinutesBefore; }
    public void setReminderMinutesBefore(int reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }

    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }

    public String getWeekStartDay() { return weekStartDay; }
    public void setWeekStartDay(String weekStartDay) { this.weekStartDay = weekStartDay; }

    public int getFocusDuration() { return focusDuration; }
    public void setFocusDuration(int focusDuration) { this.focusDuration = focusDuration; }

    public int getShortBreakDuration() { return shortBreakDuration; }
    public void setShortBreakDuration(int shortBreakDuration) { this.shortBreakDuration = shortBreakDuration; }

    public int getLongBreakDuration() { return longBreakDuration; }
    public void setLongBreakDuration(int longBreakDuration) { this.longBreakDuration = longBreakDuration; }
}
