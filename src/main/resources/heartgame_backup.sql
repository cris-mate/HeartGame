-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
--
-- Host: localhost    Database: heartgame
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `game_sessions`
--

DROP TABLE IF EXISTS `game_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_sessions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT NULL,
  `final_score` int DEFAULT '0',
  `questions_answered` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `game_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=346 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_sessions`
--

LOCK TABLES `game_sessions` WRITE;
/*!40000 ALTER TABLE `game_sessions` DISABLE KEYS */;
INSERT INTO `game_sessions` VALUES (339,549,'2025-11-19 17:46:30','2025-11-19 17:47:32',21,22),(340,550,'2025-11-19 18:05:52','2025-11-19 18:06:54',16,17),(341,551,'2025-11-19 18:08:24','2025-11-19 18:10:35',17,23),(342,552,'2025-11-19 18:11:27','2025-11-19 18:12:40',19,19),(343,553,'2025-11-20 12:26:18','2025-11-20 12:27:40',12,13),(344,554,'2025-11-20 12:28:40','2025-11-20 12:29:42',19,19),(345,555,'2025-11-20 12:31:08','2025-11-20 12:32:11',24,24);
/*!40000 ALTER TABLE `game_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logging_event`
--

DROP TABLE IF EXISTS `logging_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logging_event` (
  `timestmp` bigint NOT NULL,
  `formatted_message` text NOT NULL,
  `logger_name` varchar(254) NOT NULL,
  `level_string` varchar(254) NOT NULL,
  `thread_name` varchar(254) DEFAULT NULL,
  `reference_flag` smallint DEFAULT NULL,
  `arg0` varchar(254) DEFAULT NULL,
  `arg1` varchar(254) DEFAULT NULL,
  `arg2` varchar(254) DEFAULT NULL,
  `arg3` varchar(254) DEFAULT NULL,
  `caller_filename` varchar(254) NOT NULL,
  `caller_class` varchar(254) NOT NULL,
  `caller_method` varchar(254) NOT NULL,
  `caller_line` char(4) NOT NULL,
  `event_id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logging_event`
--

LOCK TABLES `logging_event` WRITE;
/*!40000 ALTER TABLE `logging_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `logging_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(60) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `oauth_provider` varchar(20) DEFAULT NULL,
  `oauth_id` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_oauth` (`oauth_provider`,`oauth_id`)
) ENGINE=InnoDB AUTO_INCREMENT=556 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (549,'cristidragosmatei',NULL,'cristidragosmatei@gmail.com',NULL,'google','107492280530087989909','2025-11-19 17:11:35','2025-11-19 17:46:20'),(550,'admin','$2a$10$2N3FTP3QvB.eXWvyoChhc.748pg8ZLHDbCh7bx5DaoZC9KiLta5.G','admin@heartgame.com',NULL,'password',NULL,'2025-11-19 18:05:36','2025-11-20 12:24:23'),(551,'martin','$2a$10$CuZd1q.5xmsVbkRHUF6Tqe9eADAedl6SaSiU3UIzB0YLk.Jv45th6','martin@heartgame.com',NULL,'password',NULL,'2025-11-19 18:08:08','2025-11-19 18:08:18'),(552,'serena','$2a$10$SGJ9hf0MFotziIYM4PNX7e0RcQsDWmb4Z5bngFedm7g65G/9aOWoO','serena@heartgame.com',NULL,'password',NULL,'2025-11-19 18:11:12','2025-11-19 18:11:21'),(553,'theo','$2a$10$fldIQbI/EigW3agUg05vj.MVjoqqrsozqQ0ffRfIdrfs5rr9KM7LO','theo@heartgame.com',NULL,'password',NULL,'2025-11-20 12:26:01','2025-11-20 12:26:17'),(554,'cassandra','$2a$10$XdWlT0tT2Az77K.jCRJcwOoXN0t.qqiZTBXqbDU.KMqCL13AQP456','cassandra@heartgame.com',NULL,'password',NULL,'2025-11-20 12:28:29','2025-11-20 12:28:38'),(555,'richard','$2a$10$6r/VTa0.y5LroG65bRxyce2WBSJ03nPYnklgFI3KvMpwxuxol3TQq','richard@heartgame.com',NULL,'password',NULL,'2025-11-20 12:30:57','2025-11-20 12:31:06');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-20 12:33:27
