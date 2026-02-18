package com.stahhl.bookapi.graphql.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for GraphQL Author queries.
 * Tests the full HTTP stack including schema parsing, scalar coercion, and error handling.
 */
class AuthorQueryIntegrationTest : GraphQLTestBase() {

    @Nested
    inner class `author query` {

        @Test
        fun `returns author when found`() {
            // Given
            val author = createTestAuthor("Jane Austen")

            // When
            val response = executeGraphQL("""
                query {
                    author(id: "${author.id}") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(author.id.toString(), response.extractData<String>("author.id"))
            assertEquals("Jane Austen", response.extractData<String>("author.name"))
        }

        @Test
        fun `returns null when author not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                query {
                    author(id: "$nonExistentId") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertTrue(response.isNull("author"), "Expected author to be null")
        }

        @Test
        fun `returns error for invalid UUID format`() {
            // When
            val response = executeGraphQL("""
                query {
                    author(id: "not-a-valid-uuid") {
                        id
                        name
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
        fun `resolves nested books field with empty list`() {
            // Given
            val author = createTestAuthor("New Author")

            // When
            val response = executeGraphQL("""
                query {
                    author(id: "${author.id}") {
                        id
                        name
                        books {
                            id
                            title
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(author.name, response.extractData<String>("author.name"))
            
            val booksNode = response.getDataNode("author.books")
            assertNotNull(booksNode, "Expected books field to be present")
            assertTrue(booksNode.isArray, "Expected books to be an array")
            assertEquals(0, booksNode.size(), "Expected empty books array")
        }

        @Test
        fun `resolves nested books field with books`() {
            // Given
            val author = createTestAuthor("Prolific Author")
            val book1 = createTestBook(
                title = "First Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )
            val book2 = createTestBook(
                title = "Second Book",
                isbn = VALID_ISBN_10,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    author(id: "${author.id}") {
                        id
                        name
                        books {
                            id
                            title
                            isbn
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(author.name, response.extractData<String>("author.name"))
            
            val booksNode = response.getDataNode("author.books")
            assertNotNull(booksNode)
            assertEquals(2, booksNode.size(), "Expected 2 books")
            
            val titles = booksNode.map { it.get("title").asText() }
            assertTrue(titles.contains("First Book"), "Expected 'First Book' in results")
            assertTrue(titles.contains("Second Book"), "Expected 'Second Book' in results")
        }
    }

    @Nested
    inner class `authorByName query` {

        @Test
        fun `returns author when found by name`() {
            // Given
            val author = createTestAuthor("Ernest Hemingway")

            // When
            val response = executeGraphQL("""
                query {
                    authorByName(name: "Ernest Hemingway") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(author.id.toString(), response.extractData<String>("authorByName.id"))
            assertEquals("Ernest Hemingway", response.extractData<String>("authorByName.name"))
        }

        @Test
        fun `returns null when name not found`() {
            // When
            val response = executeGraphQL("""
                query {
                    authorByName(name: "Non Existent Author") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertTrue(response.isNull("authorByName"), "Expected authorByName to be null")
        }

        @Test
        fun `finds author with special characters in name`() {
            // Given
            val author = createTestAuthor("José García-Márquez")

            // When
            val response = executeGraphQL("""
                query {
                    authorByName(name: "José García-Márquez") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(author.id.toString(), response.extractData<String>("authorByName.id"))
            assertEquals("José García-Márquez", response.extractData<String>("authorByName.name"))
        }

        @Test
        fun `returns null for empty name search`() {
            // When
            val response = executeGraphQL("""
                query {
                    authorByName(name: "") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors for empty name search")
            assertTrue(response.isNull("authorByName"), "Expected authorByName to be null for empty name")
        }

        @Test
        fun `is case sensitive`() {
            // Given
            val author = createTestAuthor("Mark Twain")

            // When - search with different case
            val response = executeGraphQL("""
                query {
                    authorByName(name: "mark twain") {
                        id
                        name
                    }
                }
            """)

            // Then - name search is likely case-sensitive, so should return null
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            // Note: This test documents the current behavior - adjust if case-insensitive search is implemented
            assertTrue(response.isNull("authorByName"), "Expected case-sensitive search to return null")
        }
    }

    @Nested
    inner class `authors query` {

        @Test
        fun `returns empty list when no authors exist`() {
            // When
            val response = executeGraphQL("""
                query {
                    authors {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val authorsNode = response.getDataNode("authors")
            assertNotNull(authorsNode, "Expected authors to be present")
            assertTrue(authorsNode.isArray, "Expected authors to be an array")
            assertEquals(0, authorsNode.size(), "Expected empty authors array")
        }

        @Test
        fun `returns all authors`() {
            // Given
            val author1 = createTestAuthor("Author One")
            val author2 = createTestAuthor("Author Two")
            val author3 = createTestAuthor("Author Three")

            // When
            val response = executeGraphQL("""
                query {
                    authors {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val authorsNode = response.getDataNode("authors")
            assertNotNull(authorsNode, "Expected authors to be present")
            assertEquals(3, authorsNode.size(), "Expected 3 authors")

            val names = authorsNode.map { it.get("name").asText() }
            assertTrue(names.contains("Author One"), "Expected 'Author One' in results")
            assertTrue(names.contains("Author Two"), "Expected 'Author Two' in results")
            assertTrue(names.contains("Author Three"), "Expected 'Author Three' in results")
        }

        @Test
        fun `returns authors with nested books`() {
            // Given
            val author1 = createTestAuthor("Author With Books")
            val author2 = createTestAuthor("Author Without Books")
            createTestBook(
                title = "A Great Book",
                isbn = VALID_ISBN_13,
                authorId = author1.id
            )

            // When
            val response = executeGraphQL("""
                query {
                    authors {
                        id
                        name
                        books {
                            title
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val authorsNode = response.getDataNode("authors")
            assertNotNull(authorsNode)
            assertEquals(2, authorsNode.size())

            // Find author with books
            val authorWithBooks = authorsNode.find { it.get("name").asText() == "Author With Books" }
            assertNotNull(authorWithBooks, "Expected to find 'Author With Books'")
            assertEquals(1, authorWithBooks.get("books").size())
            assertEquals("A Great Book", authorWithBooks.get("books")[0].get("title").asText())

            // Find author without books
            val authorWithoutBooks = authorsNode.find { it.get("name").asText() == "Author Without Books" }
            assertNotNull(authorWithoutBooks, "Expected to find 'Author Without Books'")
            assertEquals(0, authorWithoutBooks.get("books").size())
        }

        @Test
        fun `returns only requested fields`() {
            // Given
            createTestAuthor("Test Author")

            // When - only request name
            val response = executeGraphQL("""
                query {
                    authors {
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            val authorsNode = response.getDataNode("authors")
            assertNotNull(authorsNode)
            assertEquals(1, authorsNode.size())
            assertEquals("Test Author", authorsNode[0].get("name").asText())
            // id should not be present since we didn't request it
            assertTrue(authorsNode[0].get("id") == null, "Expected id to not be present when not requested")
        }
    }

    @Nested
    inner class `deeply nested queries` {

        @Test
        fun `resolves author to books to author circular reference`() {
            // Given
            val author = createTestAuthor("Circular Author")
            createTestBook(
                title = "Circular Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When - query with circular reference
            val response = executeGraphQL("""
                query {
                    author(id: "${author.id}") {
                        name
                        books {
                            title
                            author {
                                name
                            }
                        }
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals("Circular Author", response.extractData<String>("author.name"))
            
            val booksNode = response.getDataNode("author.books")
            assertNotNull(booksNode)
            assertEquals(1, booksNode.size())
            assertEquals("Circular Book", booksNode[0].get("title").asText())
            assertEquals("Circular Author", booksNode[0].get("author").get("name").asText())
        }
    }
}
