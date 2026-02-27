# Architecture Guide

This document describes the architecture patterns, conventions, and practices used in this codebase. It's intended to help developers (and AI agents) understand how to work within this project effectively.

---

## Overview

This project follows **Hexagonal Architecture** (Ports & Adapters) with a strong emphasis on keeping the domain layer pure and free from infrastructure concerns.

```
┌─────────────────────────────────────────────────────────────┐
│                      GraphQL Layer                          │
│              (Types, Queries, Mutations)                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│         (Types, Errors, Repository Interfaces)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                      │
│          (JPA Entities, Repository Implementations)         │
└─────────────────────────────────────────────────────────────┘
```

---

## Directory Structure

```
src/main/kotlin/com/stahhl/bookapi/
├── domain/
│   ├── types/           # Pure domain types (Book, Author)
│   ├── scalars/         # Value classes (IdScalar, IsbnScalar)
│   ├── errors/          # Sealed error interfaces
│   └── repositories/    # Repository interfaces (ports)
├── infrastructure/
│   ├── persistence/     # JPA entities and repository implementations
│   └── web/             # REST controllers (binary/file transport concerns)
└── graphql/
    ├── types/           # GraphQL type representations
    ├── queries/         # Query resolvers
    ├── mutations/       # Mutation resolvers
    ├── scalars/         # Custom GraphQL scalar definitions
    └── config/          # GraphQL configuration
```

---

## Layer Responsibilities

### Domain Layer (`domain/`)

The domain layer is the **core** of the application. It must remain **pure** - no framework annotations, no JPA, no GraphQL.

| Component | Purpose | Example |
|-----------|---------|---------|
| `types/` | Business entities with validation | `Book`, `Author` |
| `scalars/` | Value classes for type safety | `IdScalar`, `IsbnScalar` |
| `errors/` | Typed errors for each domain | `BookError`, `AuthorError` |
| `repositories/` | Interfaces (ports) for persistence | `BookRepository` |

**Key rules:**
- Domain types have **private constructors** with factory methods for validation
- All validation happens in the domain layer
- Repository interfaces return `Either<Error, T>` for error handling
- No JPA annotations, no Spring annotations

### Infrastructure Layer (`infrastructure/`)

The infrastructure layer contains **adapters** that implement domain ports.

| Component | Purpose | Example |
|-----------|---------|---------|
| `*Entity.kt` | JPA entities for persistence | `BookEntity`, `AuthorEntity` |
| `SpringData*Repository.kt` | Spring Data JPA interfaces | `SpringDataBookRepository` |
| `Jpa*Repository.kt` | Adapter implementing domain port | `JpaBookRepository` |
| `web/*Controller.kt` | REST endpoints for transport concerns | `BookCoverController` |

**Key rules:**
- Entities are separate from domain types
- Entities have `toDomain()` and `from()` mapping methods
- `toDomain()` returns `Either<Error, DomainType>` to handle validation errors
- Spring Data interfaces are internal implementation details

### GraphQL Layer (`graphql/`)

The GraphQL layer exposes the API and maps between GraphQL and domain types.

| Component | Purpose | Example |
|-----------|---------|---------|
| `types/` | GraphQL representations | `BookType` |
| `queries/` | Query resolvers | `BookQuery` |
| `scalars/` | Custom scalar converters | `BookIdGraphQLScalar` |

**Key rules:**
- GraphQL types are separate from domain types
- Query resolvers depend on domain repositories (not JPA)
- Use `getOrElse` to handle `Either` results gracefully

---

## File Upload Architecture (Book Covers)

Book cover uploads intentionally use a **hybrid API**:

- REST handles file binary transport
- GraphQL handles metadata/state association

### Request Flow

1. Client uploads binary to `POST /api/uploads/book-covers`
2. API stores file and creates staged `CoverUpload` metadata
3. API returns `uploadId`
4. Client calls GraphQL mutation `attachBookCover(bookId, uploadId, description)`
5. API validates upload state, stores cover metadata on `Book`, marks upload consumed

### Domain and Port Design

- `Book` owns optional `BookCover` metadata
- `CoverUpload` models staged upload lifecycle (`expiresAt`, `consumedAt`)
- `CoverUploadRepository` is a domain port with `Either<CoverUploadError, T>` returns

### Persistence Design

- `books` table stores cover metadata columns (`cover_storage_path`, `cover_content_type`, `cover_description`)
- `cover_uploads` table stores staged upload records and lifecycle timestamps
- `BookEntity.toDomain()` enforces cover field consistency (all null or all populated)

### Why This Fits Hexagonal Architecture

- Domain remains framework-agnostic and binary-agnostic
- REST controller is an infrastructure adapter for multipart transport
- GraphQL mutation is an API adapter for domain state transitions
- Storage mechanism can change (local disk → object store) without changing domain contracts

### Manual Testing UI

- A minimal internal browser UI exists at `GET /internal/cover-upload-test`
- It is protected by feature toggle `bookapi.upload.cover.test-ui.enabled` (default `false`)
- This keeps test tooling out of production by default while still providing easy local validation

---

## Type Mapping Patterns

This project uses **three separate type representations** for each entity:

```
Domain Type          GraphQL Type         JPA Entity
(Book)         <-->  (BookType)     <-->  (BookEntity)
   │                      │                    │
   │  Pure, validated     │  API exposure      │  Persistence
   │  No annotations      │  No Arrow types    │  JPA annotations
   └──────────────────────┴────────────────────┘
```

### Domain Type → GraphQL Type

```kotlin
// In BookType.kt
companion object {
    fun from(book: Book): BookType = BookType(
        id = book.id,
        title = book.title,
        authorId = book.authorId,
        isbn = book.isbn
    )
}
```

### Domain Type → JPA Entity

```kotlin
// In BookEntity.kt
companion object {
    fun from(book: Book): BookEntity = BookEntity(
        id = book.id.value,        // Unwrap value class
        isbn = book.isbn.value,    // Unwrap value class
        title = book.title,
        authorId = book.authorId.value
    )
}
```

### JPA Entity → Domain Type

```kotlin
// In BookEntity.kt
fun toDomain(): Either<BookError, Book> = either {
    val isbnResult = IsbnScalar.fromEither(isbn)
        .mapLeft { BookError.InvalidData("isbn", it.message) }
        .bind()

    Book.create(
        id = IdScalar.fromUUID(id),
        isbn = isbnResult,
        title = title,
        authorId = IdScalar.fromUUID(authorId),
    )
}
```

**Important:** `toDomain()` returns `Either` because database data could be corrupted or invalid.

---

## Persistence Patterns

### Entity Structure

```kotlin
@Entity
@Table(name = "books")
class BookEntity(
    @Id
    val id: UUID,                    // Unwrapped from IdScalar

    @Column(nullable = false)
    val title: String,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID,              // Foreign key as UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    val author: AuthorEntity? = null // Lazy-loaded relationship
) {
    // JPA requires no-arg constructor
    protected constructor() : this(
        id = UUID.randomUUID(),
        title = "",
        authorId = UUID.randomUUID()
    )
}
```

**Key patterns:**
- Use `UUID` for IDs, not value classes (JPA can't handle inline classes)
- Foreign keys: store the raw `UUID` column + optional `@ManyToOne` for lazy loading
- Use `insertable = false, updatable = false` to avoid duplicate column mapping
- Always provide a protected no-arg constructor for JPA

### Repository Implementation

```kotlin
@Repository
class JpaBookRepository(
    private val springDataRepository: SpringDataBookRepository,
) : BookRepository {

    override fun findById(id: IdScalar): Either<BookError, Book?> {
        val entity = springDataRepository.findByIdOrNull(id.value)
            ?: return null.right()
        return entity.toDomain()
    }

    override fun findAll(): Either<BookError, List<Book>> = either {
        springDataRepository.findAll().map { it.toDomain().bind() }
    }

    override fun save(book: Book): Either<BookError, Book> {
        val entity = BookEntity.from(book)
        val saved = springDataRepository.save(entity)
        return saved.toDomain()
    }
}
```

### Testing Repositories

```kotlin
@DataJpaTest
@Import(JpaBookRepository::class, JpaAuthorRepository::class)
class JpaBookRepositoryTest {

    @Autowired
    private lateinit var bookRepository: JpaBookRepository

    @Autowired
    private lateinit var authorRepository: JpaAuthorRepository

    @Test
    fun `save and retrieve book`() {
        // Create author first (foreign key constraint)
        val author = Author.create(IdScalar.generate(), "Test Author")
        authorRepository.save(author)

        val book = Book.create(
            id = IdScalar.generate(),
            isbn = IsbnScalar.fromEither("9780451524935").getOrNull()!!,
            title = "Test Book",
            authorId = author.id
        )

        val saved = bookRepository.save(book)
        saved.shouldBeRight()

        val found = bookRepository.findById(book.id)
        found.shouldBeRight()
        found.getOrNull()?.title shouldBe "Test Book"
    }
}
```

---

## Error Handling Patterns

### Domain Errors

```kotlin
// domain/errors/DomainError.kt
sealed interface DomainError

sealed interface BookError : DomainError {
    data class NotFound(val id: IdScalar) : BookError
    data class InvalidData(val field: String, val message: String) : BookError
    data class AuthorNotFound(val authorId: IdScalar) : BookError
}

sealed interface AuthorError : DomainError {
    data class NotFound(val id: IdScalar) : AuthorError
    data class InvalidName(val message: String) : AuthorError
}
```

### Using Either in Services

```kotlin
fun createBook(input: CreateBookInput): Either<BookError, Book> = either {
    // Validate author exists
    val author = authorRepository.findById(input.authorId)
        .mapLeft { BookError.AuthorNotFound(input.authorId) }
        .bind()
        ?: raise(BookError.AuthorNotFound(input.authorId))

    // Create and save book
    val book = Book.create(
        id = IdScalar.generate(),
        isbn = input.isbn,
        title = input.title,
        authorId = author.id
    )
    bookRepository.save(book).bind()
}
```

### Handling Either in GraphQL

```kotlin
// In Query resolver
fun book(id: IdScalar): BookType? =
    bookRepository.findById(id)
        .getOrElse { null }           // Convert Left to null
        ?.let { BookType.from(it) }   // Map domain to GraphQL type

fun books(): List<BookType> =
    bookRepository.findAll()
        .getOrElse { emptyList() }    // Convert Left to empty list
        .map { BookType.from(it) }
```

---

## Adding a New Entity (Checklist)

When adding a new entity (e.g., `Publisher`), follow this checklist:

### 1. Domain Layer

- [ ] Create `domain/types/Publisher.kt`
  - Private constructor
  - `create()` factory method with validation
  - Immutable data class
- [ ] Add error type to `domain/errors/DomainError.kt`
  - `sealed interface PublisherError : DomainError`
- [ ] Create `domain/repositories/PublisherRepository.kt`
  - Interface with `Either` return types

### 2. Infrastructure Layer

- [ ] Create `infrastructure/persistence/PublisherEntity.kt`
  - JPA annotations
  - `toDomain(): Either<PublisherError, Publisher>`
  - `companion object { fun from(publisher: Publisher): PublisherEntity }`
  - Protected no-arg constructor
- [ ] Create `infrastructure/persistence/SpringDataPublisherRepository.kt`
  - Extends `JpaRepository<PublisherEntity, UUID>`
- [ ] Create `infrastructure/persistence/JpaPublisherRepository.kt`
  - Implements `PublisherRepository`
  - Annotated with `@Repository`

### 3. GraphQL Layer

- [ ] Create `graphql/types/PublisherType.kt`
  - `companion object { fun from(publisher: Publisher): PublisherType }`
- [ ] Create or update query resolver
  - Inject repository
  - Map to GraphQL types

### 4. Tests

- [ ] Unit test for domain type (`PublisherTest.kt`)
- [ ] Unit test for entity mapping (`PublisherEntityTest.kt`)
- [ ] Integration test for repository (`JpaPublisherRepositoryTest.kt`)
- [ ] Unit test for GraphQL type (`PublisherTypeTest.kt`)

### 5. Seed Data (Optional)

- [ ] Add INSERT statements to `src/main/resources/data.sql`

---

## Database Seeding

The H2 database is seeded on startup via `src/main/resources/data.sql`.

**Configuration in `application.yml`:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create              # Recreates schema on startup
    defer-datasource-initialization: true  # Run data.sql after Hibernate
  sql:
    init:
      mode: always                  # Always execute data.sql
```

**Access H2 Console:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

---

## Common Gotchas

1. **Value classes and JPA**: JPA can't handle Kotlin value classes. Always unwrap to primitives in entities.

2. **Foreign key order in seed data**: Insert parent entities before children in `data.sql`.

3. **Duplicate column mapping**: When using both a raw FK column and `@ManyToOne`, use `insertable = false, updatable = false` on the relationship.

4. **Either in tests**: Use `shouldBeRight()` and `shouldBeLeft()` from Kotest for assertions.

5. **No-arg constructor**: JPA requires a no-arg constructor. Use `protected constructor()` with default values.

6. **Repository injection**: Query resolvers depend on domain `BookRepository`, not `JpaBookRepository`. Spring injects the implementation automatically.

---

## Tech Stack Reference

| Technology | Version | Documentation |
|------------|---------|---------------|
| Kotlin | 2.x | [kotlinlang.org](https://kotlinlang.org/docs/) |
| Spring Boot | 3.5.x | [spring.io](https://docs.spring.io/spring-boot/) |
| Arrow | 2.1.0 | [docs/ARROW.md](./ARROW.md) |
| GraphQL Kotlin | 8.8.x | [docs/GRAPHQL_KOTLIN.md](./GRAPHQL_KOTLIN.md) |
| H2 Database | 2.x | [h2database.com](https://h2database.com/) |
