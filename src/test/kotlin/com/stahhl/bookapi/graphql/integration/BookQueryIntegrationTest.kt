package com.stahhl.bookapi.graphql.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for GraphQL Book queries.
 * Tests the full HTTP stack including schema parsing, scalar coercion, and error handling.
 */
class BookQueryIntegrationTest : GraphQLTestBase() {

    @Nested
    inner class `book query` {

        @Test
        fun `returns book when found`() {
            // Given
            val author = createTestAuthor("George Orwell")
            val book = createTestBook(
                title = "1984",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    book(id: "${book.id}") {
                        id
                        title
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(book.id.toString(), response.extractData<String>("book.id"))
            assertEquals("1984", response.extractData<String>("book.title"))
            assertEquals(VALID_ISBN_13, response.extractData<String>("book.isbn"))
        }

        @Test
        fun `returns null when book not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                query {
                    book(id: "$nonExistentId") {
                        id
                        title
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertTrue(response.isNull("book"), "Expected book to be null")
        }

        @Test
        fun `returns error for invalid UUID format`() {
            // When
            val response = executeGraphQL("""
                query {
                    book(id: "not-a-valid-uuid") {
                        id
                        title
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected validation error for invalid UUID")
            assertTrue(
                response.errors.any { it.message.contains("Invalid", ignoreCase = true) || it.message.contains("UUID", ignoreCase = true) },
                "Expected error message to mention invalid UUID format, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `resolves nested author field`() {
            // Given
            val author = createTestAuthor("Aldous Huxley")
            val book = createTestBook(
                title = "Brave New World",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    book(id: "${book.id}") {
                        id
                        title
                        author {
                            id
                            name
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(book.title, response.extractData<String>("book.title"))
            assertEquals(author.id.toString(), response.extractData<String>("book.author.id"))
            assertEquals(author.name, response.extractData<String>("book.author.name"))
        }
    }

    @Nested
    inner class `bookByIsbn query` {

        @Test
        fun `returns book when found by ISBN-13`() {
            // Given
            val author = createTestAuthor("Test Author")
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "$VALID_ISBN_13") {
                        id
                        title
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(book.id.toString(), response.extractData<String>("bookByIsbn.id"))
            assertEquals(book.title, response.extractData<String>("bookByIsbn.title"))
        }

        @Test
        fun `returns book when found by ISBN-10`() {
            // Given
            val author = createTestAuthor("Test Author")
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_10,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "$VALID_ISBN_10") {
                        id
                        title
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(book.id.toString(), response.extractData<String>("bookByIsbn.id"))
        }

        @Test
        fun `returns null when ISBN not found`() {
            // When - query with valid ISBN format but no matching book
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "$VALID_ISBN_13") {
                        id
                        title
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertTrue(response.isNull("bookByIsbn"), "Expected bookByIsbn to be null")
        }

        @Test
        fun `returns error for invalid ISBN format`() {
            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "invalid-isbn") {
                        id
                        title
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected validation error for invalid ISBN")
        }

        @Test
        fun `returns error for ISBN with invalid checksum`() {
            // When - valid length but wrong checksum
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "9780306406158") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected validation error for invalid ISBN checksum")
            assertTrue(
                response.errors.any { it.message.contains("checksum", ignoreCase = true) || it.message.contains("invalid", ignoreCase = true) },
                "Expected error message about checksum, got: ${response.errors.map { it.message }}"
            )
        }
    }

    @Nested
    inner class `books query` {

        @Test
        fun `returns empty list when no books exist`() {
            // When
            val response = executeGraphQL("""
                query {
                    books {
                        id
                        title
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val booksNode = response.getDataNode("books")
            assertNotNull(booksNode, "Expected books to be present")
            assertTrue(booksNode.isArray, "Expected books to be an array")
            assertEquals(0, booksNode.size(), "Expected empty books array")
        }

        @Test
        fun `returns all books`() {
            // Given
            val author = createTestAuthor("Test Author")
            val book1 = createTestBook(
                title = "Book One",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )
            val book2 = createTestBook(
                title = "Book Two",
                isbn = VALID_ISBN_10,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    books {
                        id
                        title
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val booksNode = response.getDataNode("books")
            assertNotNull(booksNode, "Expected books to be present")
            assertEquals(2, booksNode.size(), "Expected 2 books")

            val titles = booksNode.map { it.get("title").asText() }
            assertTrue(titles.contains("Book One"), "Expected 'Book One' in results")
            assertTrue(titles.contains("Book Two"), "Expected 'Book Two' in results")
        }

        @Test
        fun `returns books with nested author data`() {
            // Given
            val author = createTestAuthor("Famous Author")
            createTestBook(
                title = "Great Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    books {
                        id
                        title
                        author {
                            name
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val booksNode = response.getDataNode("books")
            assertNotNull(booksNode)
            assertEquals(1, booksNode.size())
            assertEquals("Famous Author", booksNode[0].get("author").get("name").asText())
        }
    }
}
