package com.sanjay.smartlog.service;

import com.sanjay.smartlog.entity.WorkLog;
import com.sanjay.smartlog.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for work log persistence and retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository workLogRepository;

    /**
     * Returns the active hour slot string for the current time.
     * e.g., at 10:45 → "10-11", at 14:30 → "14-15"
     */
    public String getCurrentHourSlot() {
        int hour = LocalTime.now().getHour();
        return hour + "-" + (hour + 1);
    }

    /**
     * Saves/updates a COMPLETED work log for the current hour.
     * If a log already exists for this date+slot+chatId, it is updated.
     */
    public void saveLog(String chatId, String task) {
        String slot = getCurrentHourSlot();
        LocalDate today = LocalDate.now();

        Optional<WorkLog> existing = workLogRepository
                .findByDateAndHourSlotAndChatId(today, slot, chatId);

        if (existing.isPresent()) {
            WorkLog log = existing.get();
            log.setTask(task);
            log.setStatus(WorkLog.Status.COMPLETED);
            log.setTimestamp(LocalDateTime.now());
            workLogRepository.save(log);
            log("Updated existing log for slot={}", slot);
        } else {
            WorkLog newLog = new WorkLog(
                    null, today, slot, task,
                    WorkLog.Status.COMPLETED, LocalDateTime.now(), chatId
            );
            workLogRepository.save(newLog);
            log("Saved new log for slot={}", slot);
        }
    }

    /**
     * Creates a PENDING log for the given hour slot if one doesn't exist yet.
     * Used when sending hourly reminders to pre-create pending records.
     */
    public void createPendingIfAbsent(String chatId, String hourSlot) {
        LocalDate today = LocalDate.now();
        Optional<WorkLog> existing = workLogRepository
                .findByDateAndHourSlotAndChatId(today, hourSlot, chatId);
        if (existing.isEmpty()) {
            WorkLog pending = new WorkLog(
                    null, today, hourSlot, null,
                    WorkLog.Status.PENDING, LocalDateTime.now(), chatId
            );
            workLogRepository.save(pending);
            log("Created PENDING log for slot={}", hourSlot);
        }
    }

    /**
     * Returns all PENDING logs for today for a given chat ID.
     */
    public List<WorkLog> getPendingForToday(String chatId) {
        return workLogRepository.findByDateAndStatusAndChatId(
                LocalDate.now(), WorkLog.Status.PENDING, chatId);
    }

    /**
     * Returns all logs for a given date range (used for report generation).
     */
    public List<WorkLog> getLogsForDateRange(String chatId, LocalDate startDate, LocalDate endDate) {
        return workLogRepository
                .findByDateBetweenAndChatIdOrderByDateAscHourSlotAsc(startDate, endDate, chatId);
    }

    // Helper to avoid shadowing slf4j log field name
    private void log(String msg, Object... args) {
        log.info(msg, args);
    }
}
