package com.sanjay.smartlog.repository;

import com.sanjay.smartlog.entity.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

    // Find a specific log for deduplication check
    Optional<WorkLog> findByDateAndHourSlotAndChatId(LocalDate date, String hourSlot, String chatId);

    // All logs for a given date and chat ID
    List<WorkLog> findByDateAndChatId(LocalDate date, String chatId);

    // Pending logs for today (used by /check-pending)
    List<WorkLog> findByDateAndStatusAndChatId(LocalDate date, WorkLog.Status status, String chatId);

    // All logs within a date range (used for monthly report)
    List<WorkLog> findByDateBetweenAndChatIdOrderByDateAscHourSlotAsc(
            LocalDate startDate, LocalDate endDate, String chatId);
}
