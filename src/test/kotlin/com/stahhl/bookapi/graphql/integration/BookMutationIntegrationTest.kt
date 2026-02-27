package com.stahhl.bookapi.graphql.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for GraphQL Book mutations.
 * Tests create, update, and delete operations including validation and error handling.
 */
class BookMutationIntegrationTest : GraphQLTestBase() {

    private lateinit var defaultAuthor: com.stahhl.bookapi.domain.types.Author

    @BeforeEach
    fun setupAuthor() {
        defaultAuthor = createTestAuthor("Default Test Author")
    }

    @Nested
    inner class `createBook mutation` {

        @Test
        fun `creates book successfully`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_13",
                        title: "New Book",
                        authorId: "${defaultAuthor.id}"
                    ) {
                        id
                        title
                        isbn
                        author {
                            name
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertNotNull(response.extractData<String>("createBook.id"), "Expected book id to be returned")
            assertEquals("New Book", response.extractData<String>("createBook.title"))
            assertEquals(VALID_ISBN_13, response.extractData<String>("createBook.isbn"))
            assertEquals(defaultAuthor.name, response.extractData<String>("createBook.author.name"))
        }

        @Test
        fun `creates book with ISBN-10`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_10",
                        title: "ISBN-10 Book",
                        authorId: "${defaultAuthor.id}"
                    ) {
                        id
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            // ISBN-10 is stored uppercase
            assertEquals(VALID_ISBN_10.uppercase(), response.extractData<String>("createBook.isbn"))
        }

        @Test
        fun `returns error when author not found`() {
            // Given
            val nonExistentAuthorId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_13",
                        title: "Orphan Book",
                        authorId: "$nonExistentAuthorId"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for non-existent author")
            assertTrue(
                response.errors.any { it.message.contains("Author", ignoreCase = true) && it.message.contains("not found", ignoreCase = true) },
                "Expected error message about author not found, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for invalid ISBN format`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "invalid-isbn",
                        title: "Invalid ISBN Book",
                        authorId: "${defaultAuthor.id}"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ISBN")
        }

        @Test
        fun `returns error for invalid ISBN checksum`() {
            // When - valid format but wrong checksum
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "9780306406158",
                        title: "Bad Checksum Book",
                        authorId: "${defaultAuthor.id}"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ISBN checksum")
        }

        @Test
        fun `returns error for empty title`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_13",
                        title: "",
                        authorId: "${defaultAuthor.id}"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for empty title")
            assertTrue(
                response.errors.any { it.message.contains("title", ignoreCase = true) || it.message.contains("blank", ignoreCase = true) || it.message.contains("empty", ignoreCase = true) },
                "Expected error message about empty title, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for blank title`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_13",
                        title: "   ",
                        authorId: "${defaultAuthor.id}"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for blank title")
        }

        @Test
        fun `returns error for invalid author ID format`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_13",
                        title: "Test Book",
                        authorId: "not-a-uuid"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid author ID format")
        }
    }

    @Nested
    inner class `updateBook mutation` {

        @Test
        fun `updates book title successfully`() {
            // Given
            val book = createTestBook(
                title = "Original Title",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "${book.id}",
                        title: "Updated Title"
                    ) {
                        id
                        title
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(book.id.toString(), response.extractData<String>("updateBook.id"))
            assertEquals("Updated Title", response.extractData<String>("updateBook.title"))
            // ISBN should remain unchanged
            assertEquals(VALID_ISBN_13, response.extractData<String>("updateBook.isbn"))
        }

        @Test
        fun `updates book ISBN successfully`() {
            // Given
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "${book.id}",
                        isbn: "$VALID_ISBN_10"
                    ) {
                        id
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(VALID_ISBN_10.uppercase(), response.extractData<String>("updateBook.isbn"))
        }

        @Test
        fun `updates book author successfully`() {
            // Given
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )
            val newAuthor = createTestAuthor("New Author")

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "${book.id}",
                        authorId: "${newAuthor.id}"
                    ) {
                        id
                        author {
                            id
                            name
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(newAuthor.id.toString(), response.extractData<String>("updateBook.author.id"))
            assertEquals(newAuthor.name, response.extractData<String>("updateBook.author.name"))
        }

        @Test
        fun `returns error when book not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "$nonExistentId",
                        title: "New Title"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for non-existent book")
            assertTrue(
                response.errors.any { it.message.contains("Book", ignoreCase = true) && it.message.contains("not found", ignoreCase = true) },
                "Expected error message about book not found, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error when new author not found`() {
            // Given
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )
            val nonExistentAuthorId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "${book.id}",
                        authorId: "$nonExistentAuthorId"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for non-existent author")
            assertTrue(
                response.errors.any { it.message.contains("Author", ignoreCase = true) && it.message.contains("not found", ignoreCase = true) },
                "Expected error message about author not found, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for invalid ISBN on update`() {
            // Given
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "${book.id}",
                        isbn: "invalid"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ISBN")
        }

        @Test
        fun `returns error for empty title on update`() {
            // Given
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    updateBook(
                        id: "${book.id}",
                        title: ""
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for empty title")
        }
    }

    @Nested
    inner class `attachBookCover mutation` {

        @Test
        fun `attaches uploaded cover successfully`() {
            // Given
            val book = createTestBook(
                title = "With Cover",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )
            val upload = createTestCoverUpload(
                originalFilename = "cover.png",
                contentType = "image/png",
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    attachBookCover(
                        bookId: "${book.id}",
                        uploadId: "${upload.id}",
                        description: "Front cover art"
                    ) {
                        id
                        coverDescription
                        coverContentType
                        coverUrl
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals("Front cover art", response.extractData<String>("attachBookCover.coverDescription"))
            assertEquals("image/png", response.extractData<String>("attachBookCover.coverContentType"))
            assertEquals("/api/books/${book.id}/cover", response.extractData<String>("attachBookCover.coverUrl"))
        }

        @Test
        fun `returns error when upload does not exist`() {
            // Given
            val book = createTestBook(
                title = "Missing Upload",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )
            val nonExistentUploadId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    attachBookCover(
                        bookId: "${book.id}",
                        uploadId: "$nonExistentUploadId",
                        description: "Front cover art"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for missing upload")
            assertTrue(
                response.errors.any { it.message.contains("upload", ignoreCase = true) && it.message.contains("not found", ignoreCase = true) },
                "Expected upload not found error, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error when upload is expired`() {
            // Given
            val book = createTestBook(
                title = "Expired Upload",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )
            val expiredUpload = createTestCoverUpload(
                expiresAt = java.time.Instant.now().minusSeconds(1),
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    attachBookCover(
                        bookId: "${book.id}",
                        uploadId: "${expiredUpload.id}",
                        description: "Front cover art"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for expired upload")
            assertTrue(
                response.errors.any { it.message.contains("expired", ignoreCase = true) },
                "Expected expired upload error, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for blank description`() {
            // Given
            val book = createTestBook(
                title = "Blank Description",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )
            val upload = createTestCoverUpload()

            // When
            val response = executeGraphQL("""
                mutation {
                    attachBookCover(
                        bookId: "${book.id}",
                        uploadId: "${upload.id}",
                        description: "   "
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for blank description")
            assertTrue(
                response.errors.any { it.message.contains("description", ignoreCase = true) || it.message.contains("blank", ignoreCase = true) },
                "Expected description validation error, got: ${response.errors.map { it.message }}"
            )
        }
    }

    @Nested
    inner class `deleteBook mutation` {

        @Test
        fun `deletes book successfully`() {
            // Given
            val book = createTestBook(
                title = "Book to Delete",
                isbn = VALID_ISBN_13,
                authorId = defaultAuthor.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    deleteBook(id: "${book.id}")
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(true, response.extractData<Boolean>("deleteBook"))

            // Verify book is actually deleted
            val verifyResponse = executeGraphQL("""
                query {
                    book(id: "${book.id}") {
                        id
                    }
                }
            """)
            assertTrue(verifyResponse.isNull("book"), "Book should be deleted")
        }

        @Test
        fun `returns false when book does not exist`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    deleteBook(id: "$nonExistentId")
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(false, response.extractData<Boolean>("deleteBook"))
        }

        @Test
        fun `returns error for invalid ID format`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    deleteBook(id: "not-a-valid-uuid")
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ID format")
        }
    }
}
