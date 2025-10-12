/**
 * HeartGame module definition
 */
odule HeartGame {
	requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires jbcrypt;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    exports com.heartgame.view;
    exports com.heartgame.controller;
    exports com.heartgame.model;
}