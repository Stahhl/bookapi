# GraphQL Kotlin Documentation

> **Version in use:** 8.8.1 (8.x.x branch)

GraphQL Kotlin is a collection of libraries built on top of [graphql-java](https://www.graphql-java.com/) that simplify running GraphQL clients and servers in Kotlin.

---

## Official Documentation

| Resource | URL |
|----------|-----|
| **Documentation Site (8.x.x)** | https://opensource.expediagroup.com/graphql-kotlin/docs/ |
| **GitHub Repository** | https://github.com/ExpediaGroup/graphql-kotlin |

---

## Server Documentation

### Core Server Concepts

| Topic | URL |
|-------|-----|
| GraphQL Server Overview | https://opensource.expediagroup.com/graphql-kotlin/docs/server/graphql-server |
| GraphQL Request Parser | https://opensource.expediagroup.com/graphql-kotlin/docs/server/graphql-request-parser |
| GraphQL Context Factory | https://opensource.expediagroup.com/graphql-kotlin/docs/server/graphql-context-factory |
| GraphQL Request Handler | https://opensource.expediagroup.com/graphql-kotlin/docs/server/graphql-request-handler |
| Subscriptions | https://opensource.expediagroup.com/graphql-kotlin/docs/server/server-subscriptions |
| Automatic Persisted Queries (APQ) | https://opensource.expediagroup.com/graphql-kotlin/docs/server/automatic-persisted-queries/ |

### Data Loaders

| Topic | URL |
|-------|-----|
| Data Loader Overview | https://opensource.expediagroup.com/graphql-kotlin/docs/server/data-loader/ |

### Spring Server (This Project)

| Topic | URL |
|-------|-----|
| Spring Server Overview | https://opensource.expediagroup.com/graphql-kotlin/docs/server/spring-server/spring-overview |
| Spring Configuration | https://opensource.expediagroup.com/graphql-kotlin/docs/server/spring-server/spring-configuration |
| Spring Schema | https://opensource.expediagroup.com/graphql-kotlin/docs/server/spring-server/spring-schema |
| Spring Subscriptions | https://opensource.expediagroup.com/graphql-kotlin/docs/server/spring-server/spring-subscriptions |

### Ktor Server (Alternative)

| Topic | URL |
|-------|-----|
| Ktor Server Overview | https://opensource.expediagroup.com/graphql-kotlin/docs/server/ktor-server/ktor-overview |

---

## Schema Generator Documentation

| Topic | URL |
|-------|-----|
| Getting Started | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/schema-generator-getting-started |
| Writing Schemas | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/schema |
| Arguments | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/arguments |
| Nullability | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/nullability |
| Scalars | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/scalars |
| Enums | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/enums |
| Lists | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/lists |
| Interfaces | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/interfaces |
| Unions | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/writing-schemas/unions |

### Customizing Schema Generation

| Topic | URL |
|-------|-----|
| Custom Type Reference | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/customizing-schemas/custom-type-reference |
| Excluding Fields | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/customizing-schemas/excluding-fields |
| Renaming Fields | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/customizing-schemas/renaming-fields |
| Directives | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/customizing-schemas/directives |
| Deprecating Fields | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/customizing-schemas/deprecating-schema |

### Apollo Federation

| Topic | URL |
|-------|-----|
| Apollo Federation Overview | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/federation/apollo-federation |
| Federation Tracing | https://opensource.expediagroup.com/graphql-kotlin/docs/schema-generator/federation/federation-tracing |

---

## Client Documentation

| Topic | URL |
|-------|-----|
| Client Overview | https://opensource.expediagroup.com/graphql-kotlin/docs/client/client-overview |
| Client Customization | https://opensource.expediagroup.com/graphql-kotlin/docs/client/client-customization |
| Client Serialization | https://opensource.expediagroup.com/graphql-kotlin/docs/client/client-serialization |

---

## Build Plugins

| Topic | URL |
|-------|-----|
| Gradle Plugin Tasks | https://opensource.expediagroup.com/graphql-kotlin/docs/plugins/gradle-plugin-tasks |
| Maven Plugin Goals | https://opensource.expediagroup.com/graphql-kotlin/docs/plugins/maven-plugin-goals |

---

## GitHub Resources

| Resource | URL |
|----------|-----|
| Main Repository | https://github.com/ExpediaGroup/graphql-kotlin |
| Examples Directory | https://github.com/ExpediaGroup/graphql-kotlin/tree/master/examples |
| Spring Server Examples | https://github.com/ExpediaGroup/graphql-kotlin/tree/master/examples/server/spring-server |
| Discussions (Q&A) | https://github.com/ExpediaGroup/graphql-kotlin/discussions |
| Issues | https://github.com/ExpediaGroup/graphql-kotlin/issues |

---

## Community

| Resource | URL |
|----------|-----|
| Kotlin Slack (#graphql-kotlin) | https://kotlinlang.slack.com/messages/graphql-kotlin/ |
| Join Kotlin Slack | https://slack.kotlinlang.org/ |

---

## Quick Reference

### Maven Coordinates

```kotlin
// Gradle Kotlin DSL
implementation("com.expediagroup:graphql-kotlin-spring-server:8.8.1")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>com.expediagroup</groupId>
    <artifactId>graphql-kotlin-spring-server</artifactId>
    <version>8.8.1</version>
</dependency>
```

### Available Modules

| Module | Description |
|--------|-------------|
| `graphql-kotlin-schema-generator` | Code-first schema generation |
| `graphql-kotlin-federation` | Apollo Federation support |
| `graphql-kotlin-spring-server` | Spring WebFlux/MVC server integration |
| `graphql-kotlin-ktor-server` | Ktor server integration |
| `graphql-kotlin-client` | Type-safe GraphQL client |
| `graphql-kotlin-gradle-plugin` | Gradle build plugin |
| `graphql-kotlin-maven-plugin` | Maven build plugin |

---

## Additional Resources

- [GraphQL Specification](https://graphql.org/)
- [graphql-java Documentation](https://www.graphql-java.com/)
- [Apollo Federation Specification](https://www.apollographql.com/docs/apollo-server/federation/federation-spec/)
- [Blogs & Videos](https://opensource.expediagroup.com/graphql-kotlin/docs/blogs-and-videos)
