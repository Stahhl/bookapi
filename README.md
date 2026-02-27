# Book API

A Kotlin Spring Boot GraphQL API demonstrating Hexagonal Architecture (Ports & Adapters) with functional error handling using Arrow.

## Features

- **GraphQL API** with GraphQL Kotlin
- **Hexagonal Architecture** - Clean separation of domain, infrastructure, and API layers
- **Functional Error Handling** - Using Arrow's `Either` type instead of exceptions
- **Type-Safe Domain Modeling** - Value classes for IDs, ISBNs, etc.
- **H2 In-Memory Database** - Pre-seeded with sample data for development

## Quick Start

```bash
# Run tests
./gradlew test

# Start the server
./gradlew bootRun
```

The server starts at http://localhost:8080

## Endpoints

| Endpoint | Description |
|----------|-------------|
| http://localhost:8080/graphiql | GraphQL Playground |
| http://localhost:8080/h2-console | H2 Database Console |
| `POST /api/uploads/book-covers` | Upload cover image binary (multipart) |
| `GET /api/books/{bookId}/cover` | Download attached book cover |
| `GET /internal/cover-upload-test` | Internal manual upload UI (feature-flagged, disabled by default) |

### H2 Console Connection

- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** (leave empty)

## Example Queries

```graphql
# Get all books
{
  books {
    id
    title
    isbn
    isbnFormatted
    authorId
  }
}

# Get a book by ID
{
  book(id: "11111111-1111-1111-1111-111111111111") {
    title
    authorId
  }
}

# Get a book by ISBN
{
  bookByIsbn(isbn: "9780743273565") {
    title
  }
}
```

## Cover Upload Workflow

Book cover uploads use a two-step flow:

1. Upload the image via REST (`POST /api/uploads/book-covers`) and receive an `uploadId`
2. Attach it in GraphQL:

```graphql
mutation {
  attachBookCover(
    bookId: "11111111-1111-1111-1111-111111111111",
    uploadId: "6b1c1bff-f1de-4d5d-9138-3e4dc1702f1d",
    description: "Front cover art"
  ) {
    id
    coverDescription
    coverContentType
    coverUrl
  }
}
```

For full details and examples, see [docs/FILE_UPLOADS.md](./docs/FILE_UPLOADS.md).

To enable the internal manual test UI locally:

```bash
BOOKAPI_UPLOAD_COVER_TEST_UI_ENABLED=true ./gradlew bootRun
```

## Project Structure

```
src/main/kotlin/com/stahhl/bookapi/
├── domain/              # Pure business logic (no framework dependencies)
│   ├── types/           # Domain entities (Book, Author)
│   ├── scalars/         # Value classes (IdScalar, IsbnScalar)
│   ├── errors/          # Typed error definitions
│   └── repositories/    # Repository interfaces (ports)
├── infrastructure/      # Framework integrations
│   ├── persistence/     # JPA entities and repository implementations
│   └── web/             # REST endpoints (uploads, internal test UI)
└── graphql/             # API layer
    ├── types/           # GraphQL type representations
    ├── queries/         # Query resolvers
    └── scalars/         # Custom GraphQL scalars
```

## Architecture

This project follows Hexagonal Architecture principles:

- **Domain Layer** - Pure Kotlin, no annotations, contains business logic and validation
- **Infrastructure Layer** - JPA entities and repository implementations (adapters)
- **GraphQL Layer** - API exposure, maps between domain and API types

For detailed architecture documentation, see [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md).

## Tech Stack

| Technology | Purpose |
|------------|---------|
| [Kotlin](https://kotlinlang.org/) | Language |
| [Spring Boot 3.x](https://spring.io/projects/spring-boot) | Application framework |
| [GraphQL Kotlin](https://opensource.expediagroup.com/graphql-kotlin/) | GraphQL server |
| [Arrow](https://arrow-kt.io/) | Functional programming (Either, validation) |
| [H2](https://h2database.com/) | In-memory database |
| [JUnit 5](https://junit.org/junit5/) + [Kotest](https://kotest.io/) | Testing |

## Documentation

- [Architecture Guide](./docs/ARCHITECTURE.md) - Patterns, conventions, and how to add new entities
- [File Uploads](./docs/FILE_UPLOADS.md) - Two-step cover upload process and API examples
- [Arrow Patterns](./docs/ARROW.md) - Functional error handling reference
- [GraphQL Kotlin](./docs/GRAPHQL_KOTLIN.md) - GraphQL Kotlin reference

## License

MIT
