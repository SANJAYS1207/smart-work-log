package com.sanjay.smartlog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs",
        indexes = {
                @Index(name = "idx_date_chatid", columnList = "date, chat_id"),
                @Index(name = "idx_status_chatid", columnList = "status, chat_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "hour_slot", nullable = false, length = 10)
    private String hourSlot; // e.g. "10-11", "14-15"

    @Column(columnDefinition = "TEXT")
    private String task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "chat_id", nullable = false, length = 50)
    private String chatId;

    public enum Status {
        PENDING, COMPLETED
    }
}
