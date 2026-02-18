package com.stahhl.bookapi.graphql.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests specifically for custom scalar validation at the GraphQL layer.
 * Tests how the GraphQL schema handles invalid inputs for IdScalar and IsbnScalar.
 */
class ScalarValidationIntegrationTest : GraphQLTestBase() {

    @Nested
    inner class `IdScalar validation` {

        @Test
        fun `accepts valid UUID format`() {
            // Given
            val author = createTestAuthor("Test Author")
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When - using valid UUID
            val response = executeGraphQL("""
                query {
                    book(id: "${book.id}") {
                        id
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors for valid UUID")
        }

        @Test
        fun `rejects empty string`() {
            // When
            val response = executeGraphQL("""
                query {
                    book(id: "") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for empty ID")
        }

        @Test
        fun `rejects malformed UUID - too short`() {
            // When
            val response = executeGraphQL("""
                query {
                    book(id: "12345") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for too short UUID")
        }

        @Test
        fun `rejects malformed UUID - wrong format`() {
            // When
            val response = executeGraphQL("""
                query {
                    book(id: "not-a-uuid-at-all") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for wrong UUID format")
        }

        @Test
        fun `rejects UUID with invalid characters`() {
            // When - 'g' is not a valid hex character
            val response = executeGraphQL("""
                query {
                    book(id: "g1234567-1234-1234-1234-123456789012") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for UUID with invalid characters")
        }

        @Test
        fun `accepts UUID with uppercase letters`() {
            // Given
            val author = createTestAuthor("Test Author")
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When - using uppercase UUID
            val response = executeGraphQL("""
                query {
                    book(id: "${book.id.toString().uppercase()}") {
                        id
                    }
                }
            """)

            // Then - UUIDs should be case-insensitive
            assertFalse(response.hasErrors, "Expected no errors for uppercase UUID")
        }
    }

    @Nested
    inner class `IsbnScalar validation` {

        @Test
        fun `accepts valid ISBN-13`() {
            // Given
            val author = createTestAuthor("Test Author")
            createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "$VALID_ISBN_13") {
                        id
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors for valid ISBN-13")
        }

        @Test
        fun `accepts valid ISBN-10`() {
            // Given
            val author = createTestAuthor("Test Author")
            createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_10,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "$VALID_ISBN_10") {
                        id
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors for valid ISBN-10")
        }

        @Test
        fun `accepts ISBN-13 with hyphens`() {
            // When - ISBN with standard hyphen formatting
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "978-0-306-40615-7") {
                        id
                    }
                }
            """)

            // Then - should parse successfully (book may not exist, but no validation error)
            assertFalse(response.hasErrors, "Expected no errors for hyphenated ISBN-13")
        }

        @Test
        fun `accepts ISBN-10 with X check digit`() {
            // Given - ISBN-10 with X check digit: 0-8044-2957-X
            val isbnWithX = "080442957X"

            // When
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$isbnWithX",
                        title: "Test Book",
                        authorId: "${createTestAuthor("Test").id}"
                    ) {
                        isbn
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors for ISBN-10 with X check digit, got: ${response.errors}")
        }

        @Test
        fun `rejects empty ISBN`() {
            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for empty ISBN")
        }

        @Test
        fun `rejects ISBN with wrong length`() {
            // When - 11 digits (neither 10 nor 13)
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "12345678901") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for wrong length ISBN")
            assertTrue(
                response.errors.any { 
                    it.message.contains("10 or 13", ignoreCase = true) || 
                    it.message.contains("characters", ignoreCase = true) 
                },
                "Expected error message about length, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `rejects ISBN-13 with invalid prefix`() {
            // When - ISBN-13 must start with 978 or 979
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "1234567890123") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for ISBN-13 with invalid prefix")
            assertTrue(
                response.errors.any { it.message.contains("978") || it.message.contains("979") },
                "Expected error message about 978/979 prefix, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `rejects ISBN-13 with invalid checksum`() {
            // When - valid format but wrong checksum (last digit should be 7, not 8)
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "9780306406158") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid checksum")
            assertTrue(
                response.errors.any { it.message.contains("checksum", ignoreCase = true) },
                "Expected error message about checksum, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `rejects ISBN-10 with invalid checksum`() {
            // When - valid format but wrong checksum
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "0306406153") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ISBN-10 checksum")
        }

        @Test
        fun `rejects ISBN with letters in wrong positions`() {
            // When - letters other than X in check digit position
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "030640615A") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for ISBN with invalid characters")
        }

        @Test
        fun `rejects completely invalid ISBN`() {
            // When
            val response = executeGraphQL("""
                query {
                    bookByIsbn(isbn: "not-an-isbn") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for completely invalid ISBN")
        }
    }

    @Nested
    inner class `error response format` {

        @Test
        fun `validation errors have proper structure`() {
            // When - trigger a validation error
            val response = executeGraphQL("""
                query {
                    book(id: "invalid-uuid") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected validation error")
            
            val error = response.errors.first()
            assertTrue(error.message.isNotBlank(), "Error should have a message")
            // Note: path may be empty for argument validation errors in GraphQL Kotlin
        }

        @Test
        fun `mutation validation errors have message`() {
            // Given
            val author = createTestAuthor("Test Author")

            // When - trigger a domain validation error
            val response = executeGraphQL("""
                mutation {
                    createBook(
                        isbn: "$VALID_ISBN_13",
                        title: "",
                        authorId: "${author.id}"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected validation error for empty title")
            assertTrue(
                response.errors.first().message.isNotBlank(),
                "Error should have a descriptive message"
            )
        }
    }
}
