package com.sanjay.smartlog.controller;

import com.sanjay.smartlog.entity.WorkLog;
import com.sanjay.smartlog.service.TelegramService;
import com.sanjay.smartlog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles hourly reminders and pending work log follow-ups.
 *
 * Cron schedule (IST):
 *  GET /send-reminder  → every hour at :30 (10:30, 11:30 ... 18:30)
 *  GET /check-pending  → every 15 minutes
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ReminderController {

    private final TelegramService telegramService;
    private final WorkLogService workLogService;

    @Value("${telegram.chat.id}")
    private String chatId;

    /**
     * Sends the hourly reminder and pre-creates a PENDING log for the current slot.
     * Called at 10:30, 11:30, ..., 18:30 daily.
     */
    @GetMapping("/send-reminder")
    public ResponseEntity<String> sendReminder() {
        String slot = workLogService.getCurrentHourSlot();
        log.info("Sending hourly reminder for slot={} to chatId={}", slot, chatId);

        // Pre-create a PENDING record so /check-pending can detect it
        workLogService.createPendingIfAbsent(chatId, slot);

        telegramService.sendMessage(chatId,
                "⏰ Reminder: Update your work log\n" +
                "📌 Current slot: <b>" + slot + "</b>\n\n" +
                "Just type what you worked on and it will be saved automatically!");

        return ResponseEntity.ok("Reminder sent for slot: " + slot);
    }

    /**
     * Re-reminds the user if the current hour slot is still PENDING.
     * Called every 15 minutes.
     */
    @GetMapping("/check-pending")
    public ResponseEntity<String> checkPending() {
        List<WorkLog> pendingLogs = workLogService.getPendingForToday(chatId);

        if (pendingLogs.isEmpty()) {
            log.info("No pending logs for chatId={}", chatId);
            return ResponseEntity.ok("No pending logs.");
        }

        log.info("{} pending slot(s) found for chatId={}", pendingLogs.size(), chatId);

        for (WorkLog pending : pendingLogs) {
            telegramService.sendMessage(chatId,
                    "⚠️ Work updation pending for this hour\n" +
                    "📌 Slot: <b>" + pending.getHourSlot() + "</b>\n\n" +
                    "Please update your work log for this slot!");
        }

        return ResponseEntity.ok(pendingLogs.size() + " pending reminder(s) sent.");
    }
}
