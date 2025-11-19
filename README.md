# HeartGame 🎮

A Java-based cardiovascular health education game featuring MVC architecture, database persistence, and OAuth 2.0 authentication.

## 📋 Project Overview

HeartGame is an interactive quiz application that tests users' knowledge about cardiovascular health. The project demonstrates advanced software engineering principles including:

- **MVC Architecture**: Clean separation of Model, View, and Controller layers
- **Event-Driven System**: Custom event bus for component communication
- **Database Integration**: MySQL persistence with DAO pattern
- **OAuth 2.0 Support**: Google OAuth authentication alongside password-based login
- **Comprehensive Testing**: JUnit tests with H2 in-memory database

## ✨ Features

- 👤 **User Authentication**: Password-based and Google OAuth 2.0 login
- 🎯 **Quiz Gameplay**: Multiple-choice questions about heart health
- 🏆 **Leaderboard**: Top 10 high scores with persistent storage
- 💾 **Data Persistence**: All users and game sessions survive application restarts
- 📊 **Game Statistics**: Track questions answered and final scores
- 🔒 **Password Security**: BCrypt hashing for secure password storage
- 📝 **Comprehensive Logging**: SLF4J/Logback integration with database appender

## 🏗️ Architecture

### Package Structure
```
com.heartgame
├── model/           # Data models (User, GameSession, Question)
├── view/            # Swing GUI components
├── controller/      # Application flow control
├── service/         # Business logic layer
├── persistence/     # Data access layer (DAOs, DatabaseManager)
├── event/           # Event system (EventBus, EventListener)
└── util/            # Utility classes
```

### Design Patterns
- **Singleton**: DatabaseManager (single connection instance)
- **DAO Pattern**: UserDAO, GameSessionDAO for data access
- **Observer Pattern**: Custom event system for component communication
- **Template Method**: BaseDAO with transaction management

### Database Schema
- **users**: User accounts (password and OAuth)
- **game_sessions**: Game history and scores
- **logging_event**: Application logs (Logback integration)

## 🚀 Getting Started

### Prerequisites
- **Java**: JDK 11 or higher
- **MySQL**: Version 5.7 or higher
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

### Dependencies (included in `lib/` folder)
- MySQL Connector/J 9.2.0
- H2 Database 2.2.224 (testing)
- Logback Classic 1.5.18
- SLF4J API 2.0.17
- jBCrypt 0.4
- Google OAuth Client 2.2.0

### Installation

1. **Clone or extract the project**
   ```bash
   cd /path/to/HeartGame
   ```

2. **Set up MySQL database**
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE heartgame;
   EXIT;
   ```

3. **Configure database credentials** (if different from defaults)

   Edit `src/main/resources/database.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/heartgame?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=root
   db.password=your_password_here
   db.driver=com.mysql.cj.jdbc.Driver
   ```

4. **Run the application**
   - **IntelliJ IDEA**: Open project and run the main class
   - **Command Line**:
     ```bash
     # Compile (ensure classpath includes lib/*.jar)
     javac -cp "lib/*:src/main/java" src/main/java/com/heartgame/**/*.java

     # Run (adjust main class as needed)
     java -cp "lib/*:src/main/java" com.heartgame.Main
     ```

5. **First run**: The application will automatically:
   - Create database tables from `schema.sql`
   - Load seed data from `heartgame_backup.sql` (5 demo users)
   - Be ready to use immediately!

### Seed User Accounts

The application comes with 5 pre-configured users:

| Username | Password | Login Type | Notes |
|----------|----------|------------|-------|
| player1 | (use registration) | Password | Standard user |
| admin | admin123 | Password | Admin account |
| martin | martin123 | Password | Test user |
| serena | serena123 | Password | Test user |
| cristidragosmatei | N/A | Google OAuth | OAuth user |

## 💾 Database Management

### Export Current Database State
```bash
./export-database.sh
```
Creates/updates `heartgame_backup.sql` with all current users and game sessions.

### Import Database Backup
```bash
./import-database.sh
```
Restores database from `heartgame_backup.sql` (⚠️ overwrites existing data).

### How Persistence Works
The application uses **conditional backup loading**:
- **First run** (empty database): Loads seed data from `heartgame_backup.sql`
- **Subsequent runs**: Skips backup loading, preserves all existing data
- **Result**: All users and game sessions persist across application restarts ✅

## 🧪 Testing

### Run Unit Tests
The project includes comprehensive unit tests using H2 in-memory database:

```bash
# Run all tests
java -cp "lib/*:test/main/java:src/main/java" org.junit.runner.JUnitCore com.heartgame.persistence.UserDAOTest

# Tests automatically use H2 in-memory database (no MySQL required)
```

### Manual Testing Checklist
- [ ] User registration works
- [ ] Login with password works
- [ ] Google OAuth login works (requires OAuth setup)
- [ ] Game play completes successfully
- [ ] Scores appear on leaderboard
- [ ] Close and restart app → users and scores persist ✅

## 📊 Project Highlights (Year-3 CS Assessment)

### Software Engineering Principles Applied

✅ **KISS (Keep It Simple, Stupid)**
- Conditional backup loading: 30-line solution to data persistence
- Simple event system without heavy frameworks

✅ **DRY (Don't Repeat Yourself)**
- BaseDAO abstract class eliminates transaction code duplication
- Reusable connection health checks in DatabaseManager

✅ **YAGNI (You Ain't Gonna Need It)**
- No over-engineered export/import systems
- Only necessary features implemented

✅ **SRP (Single Responsibility Principle)**
- Each DAO handles one entity (UserDAO, GameSessionDAO)
- DatabaseManager only manages connections
- Clear separation: Model, View, Controller

### Key Technical Achievements
1. **Robust Database Connection Management**
   - Health checks with automatic reconnection
   - Exponential backoff retry logic
   - Connection validation before operations

2. **Secure Authentication**
   - BCrypt password hashing
   - OAuth 2.0 integration
   - Session management

3. **Data Persistence Solution**
   - Solved critical data loss issue on application restart
   - Implemented conditional backup loading
   - Preserves runtime-created data while supporting seed data

4. **Production-Ready Logging**
   - SLF4J/Logback integration
   - Database logging via DBAppender
   - Clear log levels for debugging

## 📚 Documentation

- **SUBMISSION-GUIDE.md**: Complete guide for assignment submission
- **schema.sql**: Database structure definition
- **heartgame_backup.sql**: Seed data and backup file

## 🔧 Troubleshooting

### "Cannot connect to database"
- Ensure MySQL is running: `mysql.server start` (macOS) or `sudo service mysql start` (Linux)
- Check credentials in `database.properties`
- Verify database exists: `mysql -u root -p -e "SHOW DATABASES;"`

### "No seed data loaded"
- Check logs for "Database is empty. Loading initial seed data..."
- Verify `heartgame_backup.sql` exists in `src/main/resources/`
- Manually run: `mysql -u root -p heartgame < src/main/resources/heartgame_backup.sql`

### "Users disappear after restart" (FIXED)
- This issue has been resolved in the latest version
- The application now uses conditional backup loading
- Users created at runtime persist across restarts

## 📝 License

Educational project for Year-3 Computer Science coursework.

## 👨‍💻 Author

**Year-3 Computer Science Student**
Demonstrating proficiency in:
- Object-Oriented Programming (Java)
- Software Architecture (MVC, Event-Driven)
- Database Design and Integration (MySQL)
- Security Best Practices (OAuth, BCrypt)
- Software Engineering Principles (SOLID, KISS, DRY, YAGNI)

---

**Last Updated**: November 2025
**Java Version**: 11+
**Database**: MySQL 5.7+
**Build Tool**: Manual compilation with lib/ dependencies
