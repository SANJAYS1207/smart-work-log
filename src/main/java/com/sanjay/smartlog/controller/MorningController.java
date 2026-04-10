package com.sanjay.smartlog.controller;

import com.sanjay.smartlog.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sends the morning motivational message.
 *
 * Triggered by external cron at 09:00 IST daily:
 *   GET /send-morning-message
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MorningController {

    private final TelegramService telegramService;

    @Value("${telegram.chat.id}")
    private String chatId;

    @GetMapping("/send-morning-message")
    public ResponseEntity<String> sendMorningMessage() {
        log.info("Sending morning message to chatId={}", chatId);
        telegramService.sendMessage(chatId,
                "Hey Sanjay 👋 Is everything alright? This day will be wonderful 🌞\n\n" +
                "Remember to log your work every hour. Type your update anytime!\n" +
                "Type <b>report</b> to get your monthly work log Excel.");
        return ResponseEntity.ok("Morning message sent.");
    }
}
