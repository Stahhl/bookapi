package com.stahhl.bookapi.infrastructure.persistence

import com.stahhl.bookapi.domain.errors.BookError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BookEntityTest {

    private val validIsbn13 = "9780306406157"
    private val validIsbn10 = "0306406152"
    private val validAuthorId = IdScalar.random()

    private fun createValidBook(
        id: IdScalar = IdScalar.random(),
        isbn: IsbnScalar = IsbnScalar.fromUnsafe(validIsbn13),
        title: String = "The Pragmatic Programmer",
        authorId: IdScalar = validAuthorId,
    ): Book = Book.createEither(id, isbn, title, authorId).getOrNull()!!

    @Nested
    inner class `from domain Book` {
        @Test
        fun `converts Book to BookEntity preserving all fields`() {
            val book = createValidBook()
            val entity = BookEntity.from(book)

            assertEquals(book.id.value, entity.id)
            assertEquals(book.isbn.value, entity.isbn)
            assertEquals(book.title, entity.title)
            assertEquals(book.authorId.value, entity.authorId)
        }

        @Test
        fun `converts Book with ISBN-10 correctly`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn10))
            val entity = BookEntity.from(book)

            assertEquals(validIsbn10.uppercase(), entity.isbn)
        }

        @Test
        fun `converts Book with ISBN-13 correctly`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn13))
            val entity = BookEntity.from(book)

            assertEquals(validIsbn13, entity.isbn)
        }
    }

    @Nested
    inner class `toDomain` {
        @Test
        fun `converts valid BookEntity to domain Book`() {
            val id = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val entity = BookEntity(
                id = id,
                isbn = validIsbn13,
                title = "Clean Code",
                authorId = authorId,
            )

            val result = entity.toDomain()

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals(id, book.id.value)
                assertEquals(validIsbn13, book.isbn.value)
                assertEquals("Clean Code", book.title)
                assertEquals(authorId, book.authorId.value)
            }
        }

        @Test
        fun `converts BookEntity with ISBN-10 to domain Book`() {
            val entity = BookEntity(
                id = UUID.randomUUID(),
                isbn = validIsbn10,
                title = "Test Book",
                authorId = UUID.randomUUID(),
            )

            val result = entity.toDomain()

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals(validIsbn10.uppercase(), book.isbn.value)
            }
        }

        @Test
        fun `returns Left for invalid ISBN in entity`() {
            val entity = BookEntity(
                id = UUID.randomUUID(),
                isbn = "invalid-isbn",
                title = "Test Book",
                authorId = UUID.randomUUID(),
            )

            val result = entity.toDomain()

            assertTrue(result.isLeft())
            assertIs<BookError.InvalidData>(result.leftOrNull())
        }

        @Test
        fun `returns Left for blank title in entity`() {
            val entity = BookEntity(
                id = UUID.randomUUID(),
                isbn = validIsbn13,
                title = "",
                authorId = UUID.randomUUID(),
            )

            val result = entity.toDomain()

            assertTrue(result.isLeft())
            assertIs<BookError.ValidationFailed>(result.leftOrNull())
        }
    }

    @Nested
    inner class `round-trip conversion` {
        @Test
        fun `Book to Entity to Book preserves all data`() {
            val originalBook = createValidBook()

            val entity = BookEntity.from(originalBook)
            val result = entity.toDomain()

            assertTrue(result.isRight())
            result.onRight { roundTrippedBook ->
                assertEquals(originalBook.id, roundTrippedBook.id)
                assertEquals(originalBook.isbn, roundTrippedBook.isbn)
                assertEquals(originalBook.title, roundTrippedBook.title)
                assertEquals(originalBook.authorId, roundTrippedBook.authorId)
            }
        }

        @Test
        fun `round-trip preserves ISBN-10 format`() {
            val originalBook = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn10))

            val entity = BookEntity.from(originalBook)
            val result = entity.toDomain()

            assertTrue(result.isRight())
            result.onRight { roundTrippedBook ->
                assertEquals(originalBook.isbn, roundTrippedBook.isbn)
            }
        }

        @Test
        fun `round-trip preserves ISBN-13 format`() {
            val originalBook = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn13))

            val entity = BookEntity.from(originalBook)
            val result = entity.toDomain()

            assertTrue(result.isRight())
            result.onRight { roundTrippedBook ->
                assertEquals(originalBook.isbn, roundTrippedBook.isbn)
            }
        }

        @Test
        fun `round-trip with various titles`() {
            val testCases = listOf(
                "A",  // minimal
                "A".repeat(500),  // max length
                "Title with Spaces",
                "Title: With Special-Characters!",
            )

            for (title in testCases) {
                val originalBook = createValidBook(title = title)
                val entity = BookEntity.from(originalBook)
                val result = entity.toDomain()

                assertTrue(result.isRight(), "Failed for title='$title'")
                result.onRight { roundTrippedBook ->
                    assertEquals(originalBook.title, roundTrippedBook.title)
                }
            }
        }
    }
}
