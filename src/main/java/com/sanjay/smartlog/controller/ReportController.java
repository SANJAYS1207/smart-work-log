package com.sanjay.smartlog.controller;

import com.sanjay.smartlog.service.ReportService;
import com.sanjay.smartlog.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * Manual API endpoint to export work log reports.
 *
 * Usage:
 *   GET /export-report?chatId=12345&month=April%202026
 *
 * This can also be triggered manually or via cron for automated monthly exports.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final TelegramService telegramService;

    @Value("${telegram.chat.id}")
    private String defaultChatId;

    @GetMapping("/export-report")
    public ResponseEntity<String> exportReport(
            @RequestParam(required = false) String chatId,
            @RequestParam String month) {

        String targetChatId = (chatId != null && !chatId.isBlank()) ? chatId : defaultChatId;

        YearMonth yearMonth = reportService.parseMonthInput(month);
        if (yearMonth == null) {
            return ResponseEntity.badRequest().body(
                    "Invalid month format. Use: 'April 2026', 'Apr 2026', or '04 2026'");
        }

        try {
            byte[] excelBytes = reportService.generateExcel(targetChatId, yearMonth);
            String filename = "WorkLog_" + yearMonth.getMonth().name() + "_" +
                              yearMonth.getYear() + ".xlsx";
            telegramService.sendDocument(targetChatId, filename, excelBytes);
            log.info("Report sent for chatId={} month={}", targetChatId, yearMonth);
            return ResponseEntity.ok("Report sent to Telegram for: " + yearMonth);
        } catch (Exception e) {
            log.error("Failed to generate report: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Error generating report: " + e.getMessage());
        }
    }
}
