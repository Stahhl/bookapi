package com.stahhl.bookapi.domain.types

import arrow.core.raise.either
import com.stahhl.bookapi.domain.errors.BookError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BookTest {
    private val validId = IdScalar.random()
    private val validIsbn = IsbnScalar.fromUnsafe("9780306406157")
    private val validAuthorId = IdScalar.random()

    @Nested
    inner class `create with Raise context` {
        @Test
        fun `creates Book with valid data`() {
            val result =
                either {
                    Book.create(
                        id = validId,
                        isbn = validIsbn,
                        title = "The Pragmatic Programmer",
                        authorId = validAuthorId,
                    )
                }

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals(validId, book.id)
                assertEquals(validIsbn, book.isbn)
                assertEquals("The Pragmatic Programmer", book.title)
                assertEquals(validAuthorId, book.authorId)
            }
        }

        @Test
        fun `trims whitespace from title`() {
            val result =
                either {
                    Book.create(
                        id = validId,
                        isbn = validIsbn,
                        title = "  The Pragmatic Programmer  ",
                        authorId = validAuthorId,
                    )
                }

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals("The Pragmatic Programmer", book.title)
            }
        }

        @Test
        fun `rejects blank title`() {
            val result =
                either {
                    Book.create(
                        id = validId,
                        isbn = validIsbn,
                        title = "",
                        authorId = validAuthorId,
                    )
                }

            assertTrue(result.isLeft())
            assertIs<BookError.ValidationFailed>(result.leftOrNull())
            assertTrue(
                (result.leftOrNull() as BookError.ValidationFailed).errors.any {
                    it.contains("Title cannot be blank")
                },
            )
        }

        @Test
        fun `rejects whitespace-only title`() {
            val result =
                either {
                    Book.create(
                        id = validId,
                        isbn = validIsbn,
                        title = "   ",
                        authorId = validAuthorId,
                    )
                }

            assertTrue(result.isLeft())
            assertIs<BookError.ValidationFailed>(result.leftOrNull())
        }

        @Test
        fun `rejects title exceeding 500 characters`() {
            val longTitle = "A".repeat(501)
            val result =
                either {
                    Book.create(
                        id = validId,
                        isbn = validIsbn,
                        title = longTitle,
                        authorId = validAuthorId,
                    )
                }

            assertTrue(result.isLeft())
            assertIs<BookError.ValidationFailed>(result.leftOrNull())
            assertTrue(
                (result.leftOrNull() as BookError.ValidationFailed).errors.any {
                    it.contains("Title cannot exceed 500 characters")
                },
            )
        }
    }

    @Nested
    inner class `createEither` {
        @Test
        fun `returns Right for valid data`() {
            val result =
                Book.createEither(
                    id = validId,
                    isbn = validIsbn,
                    title = "Clean Code",
                    authorId = validAuthorId,
                )

            assertTrue(result.isRight())
        }

        @Test
        fun `returns Left for invalid data`() {
            val result =
                Book.createEither(
                    id = validId,
                    isbn = validIsbn,
                    title = "",
                    authorId = validAuthorId,
                )

            assertTrue(result.isLeft())
        }
    }

    @Nested
    inner class `createNew` {
        @Test
        fun `creates Book with random ID`() {
            val result1 =
                either {
                    Book.createNew(
                        isbn = validIsbn,
                        title = "Book 1",
                        authorId = validAuthorId,
                    )
                }
            val result2 =
                either {
                    Book.createNew(
                        isbn = validIsbn,
                        title = "Book 2",
                        authorId = validAuthorId,
                    )
                }

            assertTrue(result1.isRight())
            assertTrue(result2.isRight())
            assertTrue(result1.getOrNull()?.id != result2.getOrNull()?.id)
        }
    }

    @Nested
    inner class `createNewEither` {
        @Test
        fun `returns Right with random ID for valid data`() {
            val result =
                Book.createNewEither(
                    isbn = validIsbn,
                    title = "Test Book",
                    authorId = validAuthorId,
                )

            assertTrue(result.isRight())
        }

        @Test
        fun `returns Left for invalid data`() {
            val result =
                Book.createNewEither(
                    isbn = validIsbn,
                    title = "",
                    authorId = validAuthorId,
                )

            assertTrue(result.isLeft())
        }
    }

    @Nested
    inner class `update` {
        private val originalBook =
            Book
                .createEither(
                    id = validId,
                    isbn = validIsbn,
                    title = "Original Title",
                    authorId = validAuthorId,
                ).getOrNull()!!

        @Test
        fun `updates title`() {
            val result =
                either {
                    originalBook.update(title = "New Title")
                }

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals("New Title", book.title)
                assertEquals(originalBook.authorId, book.authorId)
                assertEquals(originalBook.id, book.id)
            }
        }

        @Test
        fun `updates authorId`() {
            val newAuthorId = IdScalar.random()
            val result =
                either {
                    originalBook.update(authorId = newAuthorId)
                }

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals(originalBook.title, book.title)
                assertEquals(newAuthorId, book.authorId)
            }
        }

        @Test
        fun `updates isbn`() {
            val newIsbn = IsbnScalar.fromUnsafe("0306406152")
            val result =
                either {
                    originalBook.update(isbn = newIsbn)
                }

            assertTrue(result.isRight())
            result.onRight { book ->
                assertEquals(newIsbn, book.isbn)
            }
        }

        @Test
        fun `validates updated values`() {
            val result =
                either {
                    originalBook.update(title = "")
                }

            assertTrue(result.isLeft())
            assertIs<BookError.ValidationFailed>(result.leftOrNull())
        }
    }

    @Nested
    inner class `updateEither` {
        private val originalBook =
            Book
                .createEither(
                    id = validId,
                    isbn = validIsbn,
                    title = "Original Title",
                    authorId = validAuthorId,
                ).getOrNull()!!

        @Test
        fun `returns Right for valid update`() {
            val result = originalBook.updateEither(title = "Updated Title")

            assertTrue(result.isRight())
        }

        @Test
        fun `returns Left for invalid update`() {
            val result = originalBook.updateEither(title = "")

            assertTrue(result.isLeft())
        }
    }
}
