package com.stahhl.bookapi.graphql.types

import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book
import com.stahhl.bookapi.domain.types.BookCover
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BookTypeTest {

    private val validIsbn13 = "9780306406157"
    private val validIsbn10 = "0306406152"
    private val validAuthorId = IdScalar.random()

    private fun createValidBook(
        id: IdScalar = IdScalar.random(),
        isbn: IsbnScalar = IsbnScalar.fromUnsafe(validIsbn13),
        title: String = "The Pragmatic Programmer",
        authorId: IdScalar = validAuthorId,
        cover: BookCover? = null,
    ): Book = Book.createEither(id, isbn, title, authorId, cover).getOrNull()!!

    @Nested
    inner class `from domain Book` {
        @Test
        fun `converts Book to BookType preserving all fields`() {
            val book = createValidBook()
            val bookType = BookType(book)

            assertEquals(book.id, bookType.id)
            assertEquals(book.isbn, bookType.isbn)
            assertEquals(book.title, bookType.title)
        }

        @Test
        fun `converts Book with ISBN-10 correctly`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn10))
            val bookType = BookType(book)

            assertEquals(book.isbn, bookType.isbn)
        }

        @Test
        fun `converts Book with ISBN-13 correctly`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn13))
            val bookType = BookType(book)

            assertEquals(book.isbn, bookType.isbn)
        }

        @Test
        fun `exposes cover metadata when present`() {
            val cover = BookCover.createEither(
                storagePath = "/tmp/covers/cover.png",
                contentType = "image/png",
                description = "Cover text",
            ).getOrNull()!!
            val book = createValidBook(cover = cover)
            val bookType = BookType(book)

            assertEquals("Cover text", bookType.coverDescription)
            assertEquals("image/png", bookType.coverContentType)
            assertEquals("/api/books/${book.id}/cover", bookType.coverUrl)
        }
    }

    @Nested
    inner class `field preservation` {
        @Test
        fun `preserves title with special characters`() {
            val title = "C++: The Complete Reference"
            val book = createValidBook(title = title)
            val bookType = BookType(book)

            assertEquals(title, bookType.title)
        }

        @Test
        fun `preserves maximum length title`() {
            val title = "A".repeat(500)
            val book = createValidBook(title = title)
            val bookType = BookType(book)

            assertEquals(title, bookType.title)
        }
    }
}
