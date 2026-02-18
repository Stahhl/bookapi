# Agent Instructions

This project is a Kotlin Spring Boot API using Hexagonal Architecture with GraphQL.

## Quick Rules

- **Domain types are pure** - No JPA or framework annotations in `domain/`
- **Three type layers** - Domain types, JPA entities, and GraphQL types are separate
- **Either for errors** - Repository methods return `Either<Error, T>`, not exceptions
- **Value classes unwrap for JPA** - Use `.value` when mapping to entities
- **`toDomain()` returns Either** - Database data could be invalid

## Key Patterns

```kotlin
// Domain → Entity
BookEntity.from(book)  // book.id.value unwraps IdScalar

// Entity → Domain  
entity.toDomain()  // Returns Either<BookError, Book>

// Domain → GraphQL
BookType.from(book)

// Either in resolvers
repository.findById(id).getOrElse { null }?.let { BookType.from(it) }
```

## Directory Structure

```
domain/types/        # Pure domain types (Book, Author)
domain/scalars/      # Value classes (IdScalar, IsbnScalar)  
domain/errors/       # Sealed error interfaces
domain/repositories/ # Repository interfaces (ports)
infrastructure/      # JPA entities + repository implementations
graphql/types/       # GraphQL representations
graphql/queries/     # Query resolvers
```

## Adding New Entities

See the checklist in [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md#adding-a-new-entity-checklist).

## Detailed Documentation

- [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) - Architecture patterns, type mappings, persistence
- [docs/ARROW.md](./docs/ARROW.md) - Arrow functional programming patterns
- [docs/GRAPHQL_KOTLIN.md](./docs/GRAPHQL_KOTLIN.md) - GraphQL Kotlin reference

## Commands

```bash
./gradlew test        # Run tests
./gradlew bootRun     # Start server (http://localhost:8080)
```

GraphiQL: http://localhost:8080/graphiql  
H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`)
