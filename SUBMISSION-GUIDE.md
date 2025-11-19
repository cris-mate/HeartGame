# HeartGame - Assignment Submission Guide

This guide explains how to properly save your database and export your project for assignment submission.

---

## Part 1: Saving Your Database

### Option A: Automatic Persistence (Recommended)
Your database **automatically saves** all user data and game sessions. No manual action needed!

- **When you close the app:** All data is saved in MySQL
- **When you restart:** All users and scores are preserved
- **How it works:** The conditional backup loading system only loads seed data on first run

### Option B: Export Current Database State
If you want to capture the current database state (with all your demo users and scores):

```bash
./export-database.sh
```

This will:
- Create/update `heartgame_backup.sql` with current data
- Include all users, game sessions, and logs
- Automatically be loaded on fresh installations

**When to use this:**
- Before submitting your assignment (to include demo data)
- After adding important test users
- To create a snapshot of your leaderboard

### Testing the Backup/Restore
```bash
# Export current state
./export-database.sh

# (Optional) Test import
./import-database.sh
```

---

## Part 2: Exporting Project for Assignment Submission

### Method 1: IntelliJ IDEA Built-in Export (Recommended)

#### Step 1: Clean Your Project
1. In IntelliJ, go to **File → Project Structure → Project Settings → Modules**
2. Note your project structure
3. Close IntelliJ

#### Step 2: Export via IntelliJ
**Option A: Using File Menu**
1. Open IntelliJ IDEA
2. Go to **File → Export → Project to ZIP**
3. Choose location and name (e.g., `HeartGame-Assignment.zip`)
4. IntelliJ will automatically exclude build artifacts and IDE files

**Option B: Using Export Project Dialog**
1. **File → Manage IDE Settings → Export Settings** (if available)
2. Or use **File → Export to ZIP File**

#### Step 3: Verify Contents
Extract the ZIP and verify it includes:
- ✅ `src/` directory (all source code)
- ✅ `test/` directory (unit tests)
- ✅ `lib/` directory (JAR dependencies)
- ✅ `heartgame_backup.sql` (database backup)
- ✅ `export-database.sh` and `import-database.sh`
- ✅ `README.md` or documentation
- ✅ `.gitignore`
- ❌ NOT `.idea/` (IntelliJ project files - optional)
- ❌ NOT `out/` or `target/` (compiled binaries)
- ❌ NOT `.git/` (git repository - unless required)

---

### Method 2: Manual ZIP Creation (More Control)

#### Step 1: Use Terminal/Command Line
```bash
# Navigate to project parent directory
cd /home/user

# Create ZIP excluding unnecessary files
zip -r HeartGame-Assignment.zip HeartGame \
    -x "HeartGame/.git/*" \
    -x "HeartGame/.idea/*" \
    -x "HeartGame/out/*" \
    -x "HeartGame/target/*" \
    -x "HeartGame/*.iml" \
    -x "HeartGame/.DS_Store" \
    -x "HeartGame/bin/*"
```

#### Step 2: Verify ZIP Contents
```bash
unzip -l HeartGame-Assignment.zip | head -20
```

---

### Method 3: Using IntelliJ Project Export Feature

1. **Right-click project root** in Project Explorer
2. Select **Show in Files** or **Reveal in Finder**
3. Go up one directory level (to parent of HeartGame)
4. **Right-click HeartGame folder** → **Compress** (macOS) or **Send to → Compressed folder** (Windows)
5. Rename to `HeartGame-Assignment.zip`

**Then manually clean up:**
- Delete `.git/` folder if present
- Delete `.idea/` folder if not needed
- Delete `out/` or `target/` folders

---

## Recommended Submission Structure

Your final ZIP should contain:

```
HeartGame-Assignment.zip
├── src/
│   ├── main/
│   │   ├── java/com/heartgame/
│   │   │   ├── model/
│   │   │   ├── view/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── persistence/
│   │   │   ├── event/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── database.properties
│   │       ├── schema.sql
│   │       ├── heartgame_backup.sql
│   │       └── logback.xml
├── test/
│   └── main/
│       ├── java/com/heartgame/
│       └── resources/
├── lib/
│   ├── mysql-connector-j-9.2.0.jar
│   ├── h2-2.2.224.jar
│   └── (other dependencies)
├── export-database.sh
├── import-database.sh
├── README.md
├── SUBMISSION-GUIDE.md
└── .gitignore
```

---

## Pre-Submission Checklist

Before creating your final submission ZIP:

### Database
- [ ] Run the application and create demo users
- [ ] Play some games to populate leaderboard
- [ ] Run `./export-database.sh` to capture current state
- [ ] Verify `heartgame_backup.sql` is updated

### Code Quality
- [ ] All code compiles without errors
- [ ] Remove any `System.out.println()` debug statements
- [ ] Ensure proper logging with SLF4J/Logback
- [ ] Code follows Java naming conventions
- [ ] Comments are clear and professional

### Documentation
- [ ] README.md explains how to run the project
- [ ] Database setup instructions included
- [ ] Dependencies listed (MySQL, Java version)
- [ ] Screenshots of running application (optional)

### Testing
- [ ] Extract your ZIP to a clean directory
- [ ] Try to run the project from the extracted files
- [ ] Verify database initializes with seed data
- [ ] Test user registration and game play

---

## Quick Commands Summary

```bash
# Export current database state
./export-database.sh

# Test database import (optional)
./import-database.sh

# Create submission ZIP (from parent directory)
cd /home/user
zip -r HeartGame-Assignment.zip HeartGame \
    -x "HeartGame/.git/*" \
    -x "HeartGame/.idea/*" \
    -x "HeartGame/out/*" \
    -x "HeartGame/target/*"

# Verify ZIP contents
unzip -l HeartGame-Assignment.zip
```

---

## Troubleshooting

### "Database is empty after extraction"
- Make sure `heartgame_backup.sql` is included in your ZIP
- Make sure it's in `src/main/resources/` directory
- Run `./export-database.sh` before creating ZIP

### "Cannot connect to database"
- Ensure MySQL is running
- Check credentials in `database.properties`
- Verify database `heartgame` exists

### "Missing dependencies"
- Ensure `lib/` folder with all JARs is included
- Or include Maven/Gradle build files for automatic dependency management

---

## Notes for Your Instructor

**Database Persistence Implementation:**
This project implements conditional backup loading to prevent data loss on restart. The system:
- Loads seed data only on first-time setup (empty database)
- Preserves all runtime-created users and game sessions across restarts
- Uses `isDatabaseEmpty()` check before executing backup file
- Follows KISS principle with minimal code changes

**To test the application:**
1. Ensure MySQL is running on localhost:3306
2. Database will be created automatically on first run
3. Seed data (5 users, 5 game sessions) loads automatically
4. New users and scores persist across application restarts

---

**Author:** Year-3 Computer Science Student
**Project:** HeartGame - MVC Architecture with Database Integration
**Database:** MySQL with conditional seed data loading
**Architecture:** MVC, Event System, DAO Pattern, OAuth 2.0 Support
