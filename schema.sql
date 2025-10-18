-- HeartGame Database Schema

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS heartgame;
USE heartgame;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username)
);

-- Game sessions table for tracking gameplay
CREATE TABLE IF NOT EXISTS game_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    final_score INT DEFAULT 0,
    questions_answered INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Game events log table (for the logback appender)
CREATE TABLE IF NOT EXISTS logging_event (
    timestamp BIGINT NOT NULL,
    formatted_message TEXT NOT NULL,
    logger_name VARCHAR(254) NOT NULL,
    level_string VARCHAR(254) NOT NULL,
    thread_name VARCHAR(254),
    reference_flag SMALLINT,
    arg0 VARCHAR(254),
    arg1 VARCHAR(254),
    arg2 VARCHAR(254),
    arg3 VARCHAR(254),
    caller_filename VARCHAR(254) NOT NULL,
    caller_class VARCHAR(254) NOT NULL,
    caller_method VARCHAR(254) NOT NULL,
    caller_line CHAR(4) NOT NULL,
    event_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY
);

-- Insert a test user with password "password123"
-- BCrypt hash for "password123": $2a$10$xKx7KN7KvjJO3YYqXjjGG.f5JYDEyqJQrqVqP0Ry6U5YqJY2Y0N/2
INSERT INTO users (username, password_hash) VALUES
('admin', '$2a$10$xKx7KN7KvjJO3YYqXjjGG.f5JYDEyqJQrqVqP0Ry6U5YqJY2Y0N/2')
ON DUPLICATE KEY UPDATE username=username;

-- Create a demo user
INSERT INTO users (username, password_hash) VALUES
('demo', '$2a$10$xKx7KN7KvjJO3YYqXjjGG.f5JYDEyqJQrqVqP0Ry6U5YqJY2Y0N/2')
ON DUPLICATE KEY UPDATE username=username;

-- Support for Google OAuth
ALTER TABLE users
ADD COLUMN google_id VARCHAR(255) UNIQUE NULL AFTER password_hash,
ADD COLUMN email VARCHAR(255) NULL AFTER google_id,
ADD INDEX idx_google_id (google_id),
ADD INDEX idx_email (email);

-- Modify password_hash to be nullable (for Google OAuth users)
ALTER TABLE users
MODIFY COLUMN password_hash VARCHAR(60) NULL;