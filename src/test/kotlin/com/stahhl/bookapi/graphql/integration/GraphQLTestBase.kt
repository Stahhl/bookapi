package com.stahhl.bookapi.graphql.integration

import com.stahhl.bookapi.domain.repositories.AuthorRepository
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.repositories.CoverUploadRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Author
import com.stahhl.bookapi.domain.types.Book
import com.stahhl.bookapi.domain.types.CoverUpload
import com.stahhl.bookapi.infrastructure.persistence.SpringDataAuthorRepository
import com.stahhl.bookapi.infrastructure.persistence.SpringDataBookRepository
import com.stahhl.bookapi.infrastructure.persistence.SpringDataCoverUploadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Base class for GraphQL integration tests.
 * Provides WebTestClient setup, test data management, and helper methods.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class GraphQLTestBase {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var authorRepository: AuthorRepository

    @Autowired
    lateinit var springDataBookRepository: SpringDataBookRepository

    @Autowired
    lateinit var springDataAuthorRepository: SpringDataAuthorRepository

    @Autowired
    lateinit var coverUploadRepository: CoverUploadRepository

    @Autowired
    lateinit var springDataCoverUploadRepository: SpringDataCoverUploadRepository

    /**
     * Clean up the database before each test for isolation.
     */
    @BeforeEach
    fun cleanDatabase() {
        springDataCoverUploadRepository.deleteAll()
        springDataBookRepository.deleteAll()
        springDataAuthorRepository.deleteAll()
    }

    /**
     * Execute a GraphQL query/mutation and return the parsed response.
     *
     * @param query The GraphQL query or mutation string
     * @param variables Optional map of variables to pass to the query
     * @return Parsed GraphQLResponse
     */
    protected fun executeGraphQL(
        query: String,
        variables: Map<String, Any?> = emptyMap(),
    ): GraphQLResponse {
        val requestBody = buildString {
            append("""{"query":""")
            append(query.toJsonString())
            if (variables.isNotEmpty()) {
                append(""","variables":""")
                append(variables.toJson())
            }
            append("}")
        }

        val responseBody = webTestClient
            .post()
            .uri("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody ?: throw IllegalStateException("Empty response body")

        return GraphQLResponse.fromJson(responseBody)
    }

    /**
     * Create and save a test author.
     */
    protected fun createTestAuthor(name: String = "Test Author"): Author {
        val author = Author.createNewEither(name).getOrNull()
            ?: throw IllegalStateException("Failed to create test author")
        return authorRepository.save(author).getOrNull()
            ?: throw IllegalStateException("Failed to save test author")
    }

    /**
     * Create and save a test book.
     */
    protected fun createTestBook(
        title: String = "Test Book",
        isbn: String = "9780306406157", // Valid ISBN-13
        authorId: IdScalar,
    ): Book {
        val isbnScalar = IsbnScalar.fromEither(isbn).getOrNull()
            ?: throw IllegalStateException("Invalid test ISBN: $isbn")
        val book = Book.createNewEither(isbnScalar, title, authorId).getOrNull()
            ?: throw IllegalStateException("Failed to create test book")
        return bookRepository.save(book).getOrNull()
            ?: throw IllegalStateException("Failed to save test book")
    }

    /**
     * Create and save a staged cover upload.
     */
    protected fun createTestCoverUpload(
        originalFilename: String = "cover.png",
        contentType: String = "image/png",
        sizeBytes: Long = 1024,
        storagePath: String = "/tmp/bookapi-test-covers/${IdScalar.random()}.png",
        expiresAt: java.time.Instant = java.time.Instant.now().plusSeconds(3600),
    ): CoverUpload {
        val uploadedAt = if (expiresAt.isAfter(java.time.Instant.now())) {
            java.time.Instant.now()
        } else {
            expiresAt.minusSeconds(60)
        }

        val upload = CoverUpload.createReadyEither(
            id = IdScalar.random(),
            storagePath = storagePath,
            originalFilename = originalFilename,
            contentType = contentType,
            sizeBytes = sizeBytes,
            uploadedAt = uploadedAt,
            expiresAt = expiresAt,
        ).getOrNull() ?: throw IllegalStateException("Failed to create test cover upload")

        return coverUploadRepository.save(upload).getOrNull()
            ?: throw IllegalStateException("Failed to save test cover upload")
    }

    /**
     * Convert a string to a JSON-escaped string.
     */
    private fun String.toJsonString(): String {
        return "\"" + this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }

    /**
     * Simple map to JSON conversion for variables.
     */
    private fun Map<String, Any?>.toJson(): String {
        val entries = this.entries.joinToString(",") { (key, value) ->
            "\"$key\":${value.toJsonValue()}"
        }
        return "{$entries}"
    }

    private fun Any?.toJsonValue(): String = when (this) {
        null -> "null"
        is String -> "\"$this\""
        is Number -> this.toString()
        is Boolean -> this.toString()
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            (this as Map<String, Any?>).toJson()
        }
        is List<*> -> this.joinToString(",", "[", "]") { it.toJsonValue() }
        else -> "\"$this\""
    }

    companion object {
        // Valid test ISBNs for use in tests
        const val VALID_ISBN_13 = "9780306406157"
        const val VALID_ISBN_13_ALT = "9780134685991"
        const val VALID_ISBN_10 = "0306406152"
    }
}
