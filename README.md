# Code Kata: *Legacy Flight Booking System Testing*

# 🎯 Objective:

Introduce testability into an entangled legacy system responsible for managing flight bookings, pricing, and external integrations.

## 💼 Business Context:

Your company maintains a **legacy monolithic flight booking system**, originally written in a hurry for a client with ever-changing airline partnership rules. The original developers are long gone, and now you're tasked with adding **unit tests** and eventually decoupling and refactoring the system.

**Unfortunately:**

* Classes instantiate each other *directly* with `new`.
* Side effects (logging, emailing, pricing calls) happen all over the place.
* There is **no clean dependency injection**, no container, no interfaces.
* Changes require fear-driven development, unless something changes…

### What the legacy code does

The booking system coordinates:

1. **FlightAvailabilityService**: Queries seat availability.
2. **PricingEngine**: Applies dynamic pricing rules based on time, demand, and airline quirks.
3. **PartnerNotifier**: Notifies airlines about confirmed bookings with airline-specific formatting.
4. **AuditLogger**: Writes booking activity logs to disk.
5. **BookingRepository**: Saves booking data to a proprietary database (only available in production).
6. **BookingCoordinatorImpl**: The main orchestrator that coordinates all the services.

# 🏆 Challenges

## 🥉 Inject dependencies

Did you ever run into a codebase so awkward and full of hard to override dependencies that even the thought of writing a test is daunting? When the dreaded `new` keyword liters a codebase, writing tests after the fact is a nightmare. Luckily, the `ObjectFactory` can help you out.

### 🔧 Task

Use an `ObjectFactory` pattern to write a test for `BookingCoordinatorImpl.bookFlight()` that:
* Uses stubs instead of the untestable classes
* Checks that it returns the booking reference produced by the `BookingRepository`
* All *without extensive changes to the production code*.

This is **impossible** without changing the code. With an `ObjectFactory`, you can refactor the `new` calls to use `factory.create(Class<T>)` and inject test doubles that record behavior.

### 🏭 Concept: ObjectFactory

The `ObjectFactory` acts as a drop-in replacement for the `new` keyword, allowing you to control object creation in tests.

Instead of:
```java
AuditLogger logger = new AuditLoggerImpl(logDirectory, verboseMode);
```

Use:
```java
AuditLogger logger = factory.create(AuditLoggerImpl.class, logDirectory, verboseMode);
```

Or for interface types:
```java
AuditLogger logger = factory.create(AuditLogger.class, AuditLoggerImpl.class, logDirectory, verboseMode);
```

In tests, you can override what gets created:
```java
// For concrete types
factory.setAlways(AuditLoggerImpl.class, new FakeAuditLogger());
// For interface types
factory.setAlways(AuditLogger.class, new FakeAuditLogger());
// Return this fake once, then normal creation
factory.setOne(PricingEngine.class, new FakePricingEngine());
```

#### ApprovalTests.verify

When using ApprovalTests we use the `@Test` annotation with approval testing. ApprovalTests will compare the output against previously approved results stored in `.approved.txt` files.

Here is how you can call `factory.setOne()` using ApprovalTests:

```java
@Test
public void bookFlightShouldCreateBookingSuccessfully() {
    // Setup test doubles
    factory.setOne(BookingRepository.class, new BookingRepositoryStub());
    // ... setup other dependencies

    BookingCoordinatorImpl coordinator = new BookingCoordinatorImpl();
    String result = coordinator.bookFlight(/* parameters */).toString();

    Approvals.verify(result);
}
```

#### Constructor arguments

If you want to test constructor arguments make sure your test double implements a method to capture constructor parameters. You can create a simple interface for this:

```java
public interface ConstructorAware {
    void constructorCalledWith(Object... parameters);
}
```

Each parameter contains the constructor argument, allowing for better test logging and verification.

#### Singleton instance

You can either inject an instance of the factory (harder, but better long term) or use the Singleton instance:
```java
ObjectFactory.getInstance().create(YourClass.class, constructorArgs);
```

Or use a static import for cleaner syntax:
```java
import static com.yourpackage.ObjectFactory.create;

create(YourClass.class, constructorArgs);
```

## 📦 Prerequisites

This kata requires the **ApprovalTests** library (version 22.3.3 or later) which provides approval testing utilities for Java.

Add to your Maven `pom.xml`:
```xml
<dependency>
    <groupId>org.approvaltests</groupId>
    <artifactId>approvaltests</artifactId>
    <version>22.3.3</version>
    <scope>test</scope>
</dependency>
```

⚠️ **Important**: You'll need to handle test isolation and cleanup manually in Java, so consider using `@BeforeEach` and `@AfterEach` for ObjectFactory clearing.

## 🥈 Test the interactions

Now that you can inject dependencies, you have another problem: how do you implement test doubles and end up with an easy-to-read test? Setting up multiple mocks can become very time-consuming, but with a `CallLogger` it's easy.

### 🛠️ Task

Improve the test for `BookingCoordinatorImpl.bookFlight()` so that:
* It checks the booking was saved as expected.
* It checks a notification was sent to the correct place.
* It checks price calculation is correct.
* Verifies logging occurred.

Use ApprovalTests to create a comprehensive record of all method calls.

### ☎️ Concept: CallLogger / Wrapper Pattern

You can create wrapper test doubles that automatically log all method calls:

```java
@Test
public void bookFlightShouldCreateBookingSuccessfully() {
    factory.setOne(EmailService.class, new LoggingEmailServiceWrapper(new EmailServiceStub(), "📧"));

    // All method calls will be automatically logged
    BookingCoordinatorImpl coordinator = new BookingCoordinatorImpl();
    String result = coordinator.bookFlight(/* parameters */).toString();

    Approvals.verify(result);
}
```

## 🚀 Running the Tests

To run the tests:

```bash
mvn test
```

To run a specific test:

```bash
mvn test -Dtest=BookingCoordinatorTest#bookFlightShouldCreateBookingSuccessfully
```