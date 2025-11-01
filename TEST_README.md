# HeartGame JUnit Test Suite

## Overview

This document describes the comprehensive JUnit 5 test suite created for the HeartGame project.

## Test Coverage

### Service Layer Tests (4 test classes, ~140 tests)

1. **AuthenticationServiceTest.java** - 31 tests
   - Password hashing with BCrypt
   - Password verification
   - Password validation
   - Special characters, edge cases, and integration tests

2. **ScoringServiceTest.java** - 23 tests
   - Score initialization and increment
   - Event-driven score updates
   - Score reset functionality
   - Multiple scoring scenarios

3. **GameTimerTest.java** - 25 tests
   - Timer start/stop functionality
   - Countdown mechanism
   - Pause/resume operations
   - Timer expiration and event publishing

4. **GameEventManagerTest.java** - 34 tests
   - Singleton pattern
   - Event subscription/unsubscription
   - Event publishing to listeners
   - Error isolation and concurrent modification protection
   - Thread safety

### Model Layer Tests (4 test classes, ~80 tests)

5. **UserTest.java** - 25 tests
   - User constructors (password and OAuth)
   - Getters and setters
   - OAuth user detection
   - Field immutability

6. **UserSessionTest.java** - 38 tests
   - Singleton pattern
   - Login/logout functionality
   - Session validation
   - Activity tracking and session duration
   - OAuth user management

7. **GameSessionTest.java** - 28 tests
   - Game session creation
   - Duration calculation
   - Getters and setters
   - Data integrity

8. **QuestionTest.java** - 24 tests
   - Question construction with image and solution
   - Different image types and sizes
   - Field immutability
   - Edge cases

### Event System Tests (included in GameEventManagerTest)

- Event subscription and publishing
- Multiple listeners
- Error handling and isolation
- Concurrent event processing
- Memory leak prevention through unsubscription

### Utility Layer Tests (2 test classes, ~70 tests)

9. **ConfigurationManagerTest.java** - 35 tests
   - Singleton pattern
   - Property loading from application.properties
   - Default values
   - Integer property parsing
   - Configuration validation

10. **HTTPClientTest.java** - 35 tests
    - Utility class structure
    - Method signatures and exceptions
    - Error handling for invalid URLs
    - Parameter validation
    - Class design (final, static methods)

## Test Structure

```
src/test/java/com/heartgame/
├── service/
│   ├── AuthenticationServiceTest.java
│   ├── ScoringServiceTest.java
│   ├── GameTimerTest.java
├── model/
│   ├── UserTest.java
│   ├── UserSessionTest.java
│   ├── GameSessionTest.java
│   └── QuestionTest.java
├── event/
│   └── GameEventManagerTest.java
└── util/
    ├── ConfigurationManagerTest.java
    └── HTTPClientTest.java

src/test/resources/
└── application.properties (test configuration)
```

## Dependencies

The following JUnit 5 and testing dependencies have been added to the `lib/` directory:

- junit-jupiter-api-5.10.1.jar
- junit-jupiter-engine-5.10.1.jar
- junit-platform-commons-1.10.1.jar
- junit-platform-engine-1.10.1.jar
- junit-platform-console-standalone-1.10.1.jar
- opentest4j-1.3.0.jar
- apiguardian-api-1.1.2.jar
- mockito-core-5.8.0.jar (for future mocking needs)
- byte-buddy-1.14.11.jar (Mockito dependency)
- byte-buddy-agent-1.14.11.jar (Mockito dependency)
- objenesis-3.3.jar (Mockito dependency)

## Running Tests

### From IntelliJ IDEA

Since this is an IntelliJ IDEA project (HeartGame.iml):

1. Open the project in IntelliJ IDEA
2. Right-click on `src/test/java` and select "Run 'All Tests'"
3. Or right-click on individual test classes to run them separately

### From Command Line

```bash
# Compile main sources
javac -d out/production -cp "lib/*" @sources.txt

# Compile test sources
javac -d out/test -cp "lib/*:out/production" @test-sources.txt

# Run tests using JUnit Console Launcher
java -jar lib/junit-platform-console-standalone-1.10.1.jar \
  --classpath "out/production:out/test:lib/*" \
  --scan-classpath out/test
```

## Test Statistics

- **Total Test Classes**: 10
- **Total Test Methods**: ~256
- **Code Coverage Areas**:
  - Service layer: 4/6 classes (AuthenticationService, ScoringService, GameTimer, GameEventManager)
  - Model layer: 4/4 classes (User, UserSession, GameSession, Question)
  - Event system: 1/1 classes (GameEventManager)
  - Utility layer: 2/2 classes (ConfigurationManager, HTTPClient)

## Test Highlights

### Comprehensive Coverage

- **Edge Cases**: Null values, empty strings, special characters, boundary conditions
- **Integration Tests**: End-to-end scenarios combining multiple components
- **Concurrency Tests**: Thread safety for singletons and event systems
- **Error Handling**: Exception testing and graceful degradation
- **State Management**: Session lifecycle, timer states, event subscriptions

### Best Practices

- Clear test names using `@DisplayName` annotations
- Organized test methods with section comments
- Setup and teardown methods for test isolation
- Helper classes for test utilities (e.g., TestListener)
- Comprehensive assertions with descriptive messages

### Testing Patterns Used

1. **AAA Pattern**: Arrange-Act-Assert
2. **Given-When-Then**: Clear test structure
3. **Test Isolation**: Each test is independent
4. **Descriptive Names**: Tests explain what they verify
5. **Edge Case Coverage**: Boundary conditions and unusual inputs

## Future Enhancements

### Recommended Additional Tests

1. **DAO Layer Tests** (with mocked database)
   - UserDAO CRUD operations
   - GameSessionDAO queries
   - Transaction handling
   - Retry logic

2. **Controller Layer Tests** (with mocked services)
   - GameController game logic
   - LoginController authentication flows
   - HomeController navigation
   - LeaderboardController display logic

3. **API Service Tests** (with mocked HTTP)
   - HeartGameAPIService question fetching
   - GoogleAuthService OAuth flow
   - AvatarService avatar fetching

4. **Integration Tests**
   - Full game flow from login to leaderboard
   - Database integration tests
   - API integration tests

## Notes

- Tests use JUnit 5 (Jupiter) annotations and assertions
- Some tests may require adjustments for CI/CD environments
- GameTimer tests involve timing and may occasionally be flaky due to thread scheduling
- HTTPClient tests focus on structure; full integration tests would require a mock HTTP server

## Maintainability

- Tests are colocated with source code in standard Maven/Gradle structure
- Test resources mirror main resources
- Clear naming conventions make tests easy to find and understand
- Each test class focuses on a single production class
- Tests serve as living documentation of the codebase

## Contributing

When adding new features to HeartGame:

1. Write tests before or alongside feature implementation (TDD)
2. Aim for at least 80% code coverage
3. Include positive, negative, and edge case tests
4. Follow existing test patterns and naming conventions
5. Update this README when adding new test classes
