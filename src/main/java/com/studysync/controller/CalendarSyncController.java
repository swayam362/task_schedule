package com.studysync.controller;

import com.studysync.service.CalendarSyncService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*")
public class CalendarSyncController {

    private final CalendarSyncService calendarSyncService;

    public CalendarSyncController(CalendarSyncService calendarSyncService) {
        this.calendarSyncService = calendarSyncService;
    }

    @GetMapping(value = "/export.ics", produces = "text/calendar;charset=UTF-8")
    public ResponseEntity<String> exportIcs(@RequestParam(defaultValue = "user-sainath") String userId) {
        String icsContent = calendarSyncService.generateICalendar(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"studysync_schedule.ics\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(icsContent);
    }

    @GetMapping(value = "/feed.ics", produces = "text/calendar;charset=UTF-8")
    public ResponseEntity<String> liveIcsFeed(@RequestParam(defaultValue = "user-sainath") String userId) {
        String icsContent = calendarSyncService.generateICalendar(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"feed.ics\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(icsContent);
    }
}

