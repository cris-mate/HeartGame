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

    // --- JUnit 5 Test Dependencies ---
    requires org.junit.jupiter.api;

    // --- Open Packages for JUnit Testing ---
    opens com.heartgame to org.junit.platform.commons;
    opens com.heartgame.controller to org.junit.platform.commons;
    opens com.heartgame.event to org.junit.platform.commons;
    opens com.heartgame.model to org.junit.platform.commons;
    opens com.heartgame.persistence to org.junit.platform.commons;
    opens com.heartgame.service to org.junit.platform.commons;
    opens com.heartgame.util to org.junit.platform.commons;
    opens com.heartgame.view to org.junit.platform.commons;

    // Exported packages
    exports com.heartgame.view;
    exports com.heartgame.controller;
    exports com.heartgame.model;
    exports com.heartgame.service;
    exports com.heartgame.event;
    exports com.heartgame.persistence;
}