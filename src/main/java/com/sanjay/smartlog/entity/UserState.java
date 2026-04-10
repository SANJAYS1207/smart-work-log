package com.sanjay.smartlog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserState {

    @Id
    @Column(name = "chat_id", length = 50)
    private String chatId;

    /**
     * Conversation state machine values:
     * - IDLE           : normal mode, text → work log
     * - AWAITING_MONTH : waiting for user to type month/year for report
     */
    @Column(nullable = false, length = 50)
    private String state = "IDLE";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserState(String chatId) {
        this.chatId = chatId;
        this.state = "IDLE";
        this.updatedAt = LocalDateTime.now();
    }
}
