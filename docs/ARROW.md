# Arrow Functional Programming Documentation

> **Version in use:** 2.1.0 (latest stable)

Arrow is a functional programming library for Kotlin that provides typed functional programming patterns. This project uses Arrow to implement robust error handling, data validation, and functional composition patterns.

---

## Official Documentation

| Resource | URL |
|----------|-----|
| **Documentation Site** | https://arrow-kt.io/docs/ |
| **GitHub Repository** | https://github.com/arrow-kt/arrow |
| **API Reference** | https://apidocs.arrow-kt.io/ |

---

## Core Concepts Used in This Project

### Error Handling with Either

We use `Either<E, A>` for typed error handling instead of exceptions. This makes error cases explicit in function signatures.

| Topic | URL |
|-------|-----|
| Either Overview | https://arrow-kt.io/docs/typed-errors/either-and-ior/ |
| Working with Either | https://arrow-kt.io/docs/typed-errors/working-with-typed-errors/ |

```kotlin
// Example: Service returning Either
fun findBook(id: BookId): Either<BookError, Book> = either {
    val book = repository.findById(id).bind()
    book ?: raise(BookError.NotFound(id))
}
```

### Validated Data with Raise

We use the `Raise` DSL for accumulating validation errors.

| Topic | URL |
|-------|-----|
| Raise DSL | https://arrow-kt.io/docs/typed-errors/raise/ |
| Accumulating Errors | https://arrow-kt.io/docs/typed-errors/raise/#accumulating-errors |

```kotlin
// Example: Accumulating validation errors
fun validateBook(input: BookInput): Either<NonEmptyList<ValidationError>, Book> = either {
    zipOrAccumulate(
        { ensureNotBlank(input.title) { ValidationError.BlankTitle } },
        { ensureNotBlank(input.author) { ValidationError.BlankAuthor } },
        { validateIsbn(input.isbn).bind() }
    ) { title, author, isbn ->
        Book(title = title, author = author, isbn = isbn)
    }
}
```

### Nullable Handling with Option

We prefer `Option<A>` over nullable types for explicit optional semantics.

| Topic | URL |
|-------|-----|
| Option Overview | https://arrow-kt.io/docs/typed-errors/nullable-and-option/ |

```kotlin
// Example: Working with Option
fun findBookByIsbn(isbn: Isbn): Option<Book> =
    repository.findByIsbn(isbn).toOption()
```

### Resource Safety

We use Arrow's resource management for safe acquisition and release of resources.

| Topic | URL |
|-------|-----|
| Resource Safety | https://arrow-kt.io/docs/coroutines/resource-safety/ |

---

## Immutable Data Patterns

### Optics for Immutable Updates

We use Arrow Optics for working with deeply nested immutable data structures.

| Topic | URL |
|-------|-----|
| Optics Overview | https://arrow-kt.io/docs/optics/ |
| Lens | https://arrow-kt.io/docs/optics/lens/ |
| Prism | https://arrow-kt.io/docs/optics/prism/ |

```kotlin
// Example: Using optics for immutable updates
@optics
data class Book(
    val id: BookId,
    val title: String,
    val metadata: BookMetadata
) {
    companion object
}

// Update nested field immutably
val updatedBook = Book.metadata.publisher.modify(book) { it.uppercase() }
```

---

## Dependency Configuration

### Gradle Setup

```kotlin
// build.gradle.kts
dependencies {
    // Arrow Core - Either, Option, Raise DSL
    implementation("io.arrow-kt:arrow-core:2.1.0")
    
    // Arrow Fx Coroutines - Resource safety, parallel operations
    implementation("io.arrow-kt:arrow-fx-coroutines:2.1.0")
    
    // Arrow Optics - Immutable data transformations (optional)
    implementation("io.arrow-kt:arrow-optics:2.1.0")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:2.1.0")
}
```

### Maven Setup

```xml
<dependencies>
    <dependency>
        <groupId>io.arrow-kt</groupId>
        <artifactId>arrow-core</artifactId>
        <version>2.1.0</version>
    </dependency>
    <dependency>
        <groupId>io.arrow-kt</groupId>
        <artifactId>arrow-fx-coroutines</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>
```

---

## Available Modules

| Module | Description |
|--------|-------------|
| `arrow-core` | Core types: Either, Option, Raise, NonEmptyList |
| `arrow-fx-coroutines` | Functional effects with coroutines, resource safety |
| `arrow-optics` | Optics for immutable data manipulation |
| `arrow-optics-ksp-plugin` | KSP plugin for generating optics |
| `arrow-resilience` | Retry, circuit breaker, saga patterns |
| `arrow-atomic` | Atomic references for concurrent programming |

---

## Functional Patterns Reference

### Railway-Oriented Programming

All service methods return `Either<Error, Success>` to enable composition:

```kotlin
fun createBook(input: BookInput): Either<BookError, Book> = either {
    val validated = validateBook(input).bind()
    val saved = repository.save(validated).bind()
    eventPublisher.publish(BookCreated(saved)).bind()
    saved
}
```

### Error Type Hierarchy

```kotlin
sealed interface BookError {
    data class NotFound(val id: BookId) : BookError
    data class ValidationFailed(val errors: NonEmptyList<ValidationError>) : BookError
    data class DatabaseError(val cause: Throwable) : BookError
}
```

### Suspend + Either Integration

Arrow integrates seamlessly with Kotlin coroutines:

```kotlin
suspend fun fetchAndProcess(id: BookId): Either<BookError, ProcessedBook> = either {
    val book = bookService.findBook(id).bind()
    val enriched = enrichmentService.enrich(book).bind()
    processingService.process(enriched).bind()
}
```

---

## Testing

Arrow provides testing utilities for functional code:

| Topic | URL |
|-------|-----|
| Testing Guide | https://arrow-kt.io/docs/learn/typed-errors/validation/ |

```kotlin
@Test
fun `findBook returns NotFound for missing book`() {
    val result = bookService.findBook(BookId("missing"))
    
    result shouldBeLeft BookError.NotFound(BookId("missing"))
}

@Test
fun `createBook succeeds with valid input`() {
    val result = bookService.createBook(validInput)
    
    result.shouldBeRight()
    result.getOrNull()?.title shouldBe "Test Book"
}
```

---

## Migration Guide

When migrating existing code to use Arrow patterns:

1. **Replace exceptions with Either** - Convert `throw` to `raise()` in an `either {}` block
2. **Replace nullable returns with Option** - Use `.toOption()` for explicit optionality
3. **Replace mutable data with optics** - Use lenses for nested updates
4. **Accumulate validation errors** - Use `zipOrAccumulate` instead of fail-fast validation

---

## Community Resources

| Resource | URL |
|----------|-----|
| Kotlin Slack (#arrow) | https://kotlinlang.slack.com/messages/arrow/ |
| GitHub Discussions | https://github.com/arrow-kt/arrow/discussions |
| Arrow Blog | https://arrow-kt.io/blog/ |

---

## Additional Resources

- [Arrow Learn](https://arrow-kt.io/learn/)
- [Functional Error Handling in Kotlin](https://arrow-kt.io/docs/typed-errors/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
