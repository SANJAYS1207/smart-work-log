-- Smart Work Log Tracker
-- MySQL Schema Script (run once on your cloud DB)
-- Spring JPA with ddl-auto=update will also auto-create these,
-- but this script lets you review and create manually if needed.

CREATE DATABASE IF NOT EXISTS smartlog
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smartlog;

-- Work logs: one row per hour slot per day
CREATE TABLE IF NOT EXISTS work_logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    date       DATE         NOT NULL,
    hour_slot  VARCHAR(10)  NOT NULL,   -- e.g. "10-11"
    task       TEXT,
    status     ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    timestamp  DATETIME     NOT NULL,
    chat_id    VARCHAR(50)  NOT NULL,
    INDEX idx_date_chatid  (date, chat_id),
    INDEX idx_status_chatid (status, chat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User conversation state
CREATE TABLE IF NOT EXISTS user_state (
    chat_id    VARCHAR(50)  NOT NULL PRIMARY KEY,
    state      VARCHAR(50)  NOT NULL DEFAULT 'IDLE',
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
