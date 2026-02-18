package com.stahhl.bookapi.graphql.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for GraphQL Author mutations.
 * Tests create, update, and delete operations including validation and error handling.
 */
class AuthorMutationIntegrationTest : GraphQLTestBase() {

    @Nested
    inner class `createAuthor mutation` {

        @Test
        fun `creates author successfully`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createAuthor(name: "New Author") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertNotNull(response.extractData<String>("createAuthor.id"), "Expected author id")
            assertEquals("New Author", response.extractData<String>("createAuthor.name"))
        }

        @Test
        fun `creates author with books field returning empty list`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createAuthor(name: "Author Without Books") {
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
            val booksNode = response.getDataNode("createAuthor.books")
            assertNotNull(booksNode, "Expected books field to be present")
            assertTrue(booksNode.isArray, "Expected books to be an array")
            assertEquals(0, booksNode.size(), "Expected empty books array for new author")
        }

        @Test
        fun `returns error for empty name`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createAuthor(name: "") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for empty name")
            assertTrue(
                response.errors.any { 
                    it.message.contains("name", ignoreCase = true) || 
                    it.message.contains("blank", ignoreCase = true) ||
                    it.message.contains("empty", ignoreCase = true)
                },
                "Expected error message about empty name, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for blank name`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createAuthor(name: "   ") {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for blank name")
        }

        @Test
        fun `creates author with special characters in name`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    createAuthor(name: "José García-Márquez III") {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals("José García-Márquez III", response.extractData<String>("createAuthor.name"))
        }
    }

    @Nested
    inner class `updateAuthor mutation` {

        @Test
        fun `updates author name successfully`() {
            // Given
            val author = createTestAuthor("Original Name")

            // When
            val response = executeGraphQL("""
                mutation {
                    updateAuthor(
                        id: "${author.id}",
                        name: "Updated Name"
                    ) {
                        id
                        name
                    }
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(author.id.toString(), response.extractData<String>("updateAuthor.id"))
            assertEquals("Updated Name", response.extractData<String>("updateAuthor.name"))
        }

        @Test
        fun `returns error when author not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    updateAuthor(
                        id: "$nonExistentId",
                        name: "New Name"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for non-existent author")
            assertTrue(
                response.errors.any { 
                    it.message.contains("Author", ignoreCase = true) && 
                    it.message.contains("not found", ignoreCase = true) 
                },
                "Expected error message about author not found, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for empty name on update`() {
            // Given
            val author = createTestAuthor("Original Name")

            // When
            val response = executeGraphQL("""
                mutation {
                    updateAuthor(
                        id: "${author.id}",
                        name: ""
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for empty name")
        }

        @Test
        fun `returns error for invalid ID format`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    updateAuthor(
                        id: "not-a-uuid",
                        name: "New Name"
                    ) {
                        id
                    }
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ID format")
        }

        @Test
        fun `preserves author books after update`() {
            // Given
            val author = createTestAuthor("Author With Books")
            val book = createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    updateAuthor(
                        id: "${author.id}",
                        name: "Updated Author Name"
                    ) {
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
            assertEquals("Updated Author Name", response.extractData<String>("updateAuthor.name"))
            
            val booksNode = response.getDataNode("updateAuthor.books")
            assertNotNull(booksNode)
            assertEquals(1, booksNode.size(), "Expected author to still have 1 book")
            assertEquals(book.title, booksNode[0].get("title").asText())
        }
    }

    @Nested
    inner class `deleteAuthor mutation` {

        @Test
        fun `deletes author successfully when no books`() {
            // Given
            val author = createTestAuthor("Author to Delete")

            // When
            val response = executeGraphQL("""
                mutation {
                    deleteAuthor(id: "${author.id}")
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(true, response.extractData<Boolean>("deleteAuthor"))
        }

        @Test
        fun `returns false when author does not exist`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When
            val response = executeGraphQL("""
                mutation {
                    deleteAuthor(id: "$nonExistentId")
                }
            """)

            // Then
            assertFalse(response.hasErrors, "Expected no errors but got: ${response.errors}")
            assertEquals(false, response.extractData<Boolean>("deleteAuthor"))
        }

        @Test
        fun `returns error when author has books`() {
            // Given
            val author = createTestAuthor("Author With Books")
            createTestBook(
                title = "Test Book",
                isbn = VALID_ISBN_13,
                authorId = author.id
            )

            // When
            val response = executeGraphQL("""
                mutation {
                    deleteAuthor(id: "${author.id}")
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error when deleting author with books")
            assertTrue(
                response.errors.any { 
                    it.message.contains("author", ignoreCase = true) && 
                    it.message.contains("books", ignoreCase = true)
                },
                "Expected error message about author having books, got: ${response.errors.map { it.message }}"
            )
        }

        @Test
        fun `returns error for invalid ID format`() {
            // When
            val response = executeGraphQL("""
                mutation {
                    deleteAuthor(id: "not-a-valid-uuid")
                }
            """)

            // Then
            assertTrue(response.hasErrors, "Expected error for invalid ID format")
        }
    }
}
