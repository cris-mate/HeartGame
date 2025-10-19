/**
 * HeartGame module definition
 */
module HeartGame {

    // Core Java modules
    requires java.desktop;
    requires java.logging;
    requires java.sql;

    // Authentication & Security
    requires jbcrypt;

    // Logging
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;

    // JSON Processing
    requires com.google.gson;

    // Exported packages
    exports com.heartgame.view;
    exports com.heartgame.controller;
    exports com.heartgame.model;
    exports com.heartgame.service;
    exports com.heartgame.event;
    exports com.heartgame.persistence;
}