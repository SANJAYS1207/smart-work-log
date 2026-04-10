package com.sanjay.smartlog.service;

import com.sanjay.smartlog.entity.UserState;
import com.sanjay.smartlog.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * State machine for multi-step Telegram conversations.
 *
 * States:
 *   IDLE          - normal; plain text → work log
 *   AWAITING_MONTH - waiting for user to supply month/year for report
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationStateService {

    private final UserStateRepository userStateRepository;
    private final WorkLogService workLogService;
    private final ReportService reportService;
    private final TelegramService telegramService;

    @Value("${telegram.chat.id}")
    private String defaultChatId;

    /**
     * Main entry point called from WebhookController.
     * Routes message to appropriate handler based on current state.
     */
    public void handleIncoming(String chatId, String text) {
        if (text == null || text.isBlank()) return;

        String trimmed = text.trim();
        String state = getState(chatId);

        log.info("Incoming from chatId={}, state={}, text={}", chatId, state, trimmed);

        switch (state) {
            case "AWAITING_MONTH" -> handleMonthInput(chatId, trimmed);
            default -> handleIdleInput(chatId, trimmed);
        }
    }

    // -------------------------------------------------------------------------
    // State: IDLE
    // -------------------------------------------------------------------------

    private void handleIdleInput(String chatId, String text) {
        if (text.equalsIgnoreCase("report")) {
            setState(chatId, "AWAITING_MONTH");
            telegramService.sendMessage(chatId,
                    "📊 Which month and year would you like the report for?\n" +
                    "Please reply in any of these formats:\n" +
                    "• <b>April 2026</b>\n• <b>Apr 2026</b>\n• <b>04 2026</b>");
        } else {
            // Treat as work log for current hour
            workLogService.saveLog(chatId, text);
            String slot = workLogService.getCurrentHourSlot();
            telegramService.sendMessage(chatId,
                    "✅ Work log saved for <b>" + slot + "</b>!\n" +
                    "📝 <i>" + escapeHtml(text) + "</i>");
        }
    }

    // -------------------------------------------------------------------------
    // State: AWAITING_MONTH
    // -------------------------------------------------------------------------

    private void handleMonthInput(String chatId, String text) {
        YearMonth yearMonth = reportService.parseMonthInput(text);

        if (yearMonth == null) {
            telegramService.sendMessage(chatId,
                    "❌ I couldn't understand that format. Please try:\n" +
                    "• <b>April 2026</b>\n• <b>Apr 2026</b>\n• <b>04 2026</b>");
            return;
        }

        // Reset state first
        setState(chatId, "IDLE");

        telegramService.sendMessage(chatId,
                "⏳ Generating report for <b>" + yearMonth.getMonth().name() + " " +
                yearMonth.getYear() + "</b>...");

        try {
            byte[] excelBytes = reportService.generateExcel(chatId, yearMonth);
            String filename = "WorkLog_" + yearMonth.getMonth().name() + "_" +
                              yearMonth.getYear() + ".xlsx";
            telegramService.sendDocument(chatId, filename, excelBytes);
        } catch (Exception e) {
            log.error("Error generating report for chatId={}: {}", chatId, e.getMessage());
            telegramService.sendMessage(chatId,
                    "❌ Sorry, an error occurred while generating the report. Please try again.");
        }
    }

    // -------------------------------------------------------------------------
    // State helpers
    // -------------------------------------------------------------------------

    public String getState(String chatId) {
        return userStateRepository.findById(chatId)
                .map(UserState::getState)
                .orElse("IDLE");
    }

    public void setState(String chatId, String newState) {
        UserState us = userStateRepository.findById(chatId)
                .orElse(new UserState(chatId));
        us.setState(newState);
        us.setUpdatedAt(LocalDateTime.now());
        userStateRepository.save(us);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
