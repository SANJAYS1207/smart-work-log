package com.sanjay.smartlog.controller;

import com.sanjay.smartlog.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sends the end-of-day summary / check-in message.
 *
 * Triggered by external cron at 18:45 IST daily:
 *   GET /send-day-summary
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SummaryController {

    private final TelegramService telegramService;

    @Value("${telegram.chat.id}")
    private String chatId;

    @GetMapping("/send-day-summary")
    public ResponseEntity<String> sendDaySummary() {
        log.info("Sending day summary to chatId={}", chatId);
        telegramService.sendMessage(chatId,
                "How was your day Sanjay? 😊 Was today a good day?\n\n" +
                "Feel free to send your end-of-day thoughts. " +
                "Type <b>report</b> anytime to get your monthly Excel log.");
        return ResponseEntity.ok("Day summary sent.");
    }
}
