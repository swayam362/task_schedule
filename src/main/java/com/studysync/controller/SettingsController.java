package com.studysync.controller;

import com.studysync.model.UserSettings;
import com.studysync.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<UserSettings> getSettings(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(settingsService.getSettings(userId));
    }

    @PutMapping
    public ResponseEntity<UserSettings> updateSettings(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestBody UserSettings settings) {
        return ResponseEntity.ok(settingsService.updateSettings(userId, settings));
    }
}
