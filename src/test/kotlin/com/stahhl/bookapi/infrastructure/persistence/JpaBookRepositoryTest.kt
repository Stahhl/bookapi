package com.stahhl.bookapi.infrastructure.persistence

import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Author
import com.stahhl.bookapi.domain.types.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@Import(JpaBookRepository::class, JpaAuthorRepository::class)
class JpaBookRepositoryTest {

    @Autowired
    private lateinit var repository: BookRepository

    @Autowired
    private lateinit var springDataRepository: SpringDataBookRepository

    @Autowired
    private lateinit var springDataAuthorRepository: SpringDataAuthorRepository

    private val validIsbn13 = "9780306406157"
    private val validIsbn10 = "0306406152"

    private lateinit var testAuthor: Author

    private fun createValidBook(
        id: IdScalar = IdScalar.random(),
        isbn: IsbnScalar = IsbnScalar.fromUnsafe(validIsbn13),
        title: String = "The Pragmatic Programmer",
        authorId: IdScalar = testAuthor.id,
    ): Book = Book.createEither(id, isbn, title, authorId).getOrNull()!!

    private fun createAndSaveAuthor(name: String = "Test Author"): Author {
        val author = Author.createNewEither(name).getOrNull()!!
        springDataAuthorRepository.save(AuthorEntity.from(author))
        return author
    }

    @BeforeEach
    fun setUp() {
        springDataRepository.deleteAll()
        springDataAuthorRepository.deleteAll()
        testAuthor = createAndSaveAuthor("David Thomas")
    }

    @Nested
    inner class `save` {
        @Test
        fun `saves a new book and returns it`() {
            val book = createValidBook()

            val result = repository.save(book)

            assertTrue(result.isRight())
            result.onRight { savedBook ->
                assertEquals(book.id, savedBook.id)
                assertEquals(book.isbn, savedBook.isbn)
                assertEquals(book.title, savedBook.title)
                assertEquals(book.authorId, savedBook.authorId)
            }
        }

        @Test
        fun `persists book to database`() {
            val book = createValidBook()

            repository.save(book)

            val entity = springDataRepository.findById(book.id.value)
            assertTrue(entity.isPresent)
            assertEquals(book.title, entity.get().title)
        }

        @Test
        fun `updates existing book`() {
            val book = createValidBook()
            repository.save(book)

            val updatedBook = book.updateEither(title = "Updated Title").getOrNull()!!
            val result = repository.save(updatedBook)

            assertTrue(result.isRight())
            result.onRight { savedBook ->
                assertEquals("Updated Title", savedBook.title)
            }

            // Verify only one record exists
            assertEquals(1, springDataRepository.count())
        }
    }

    @Nested
    inner class `findById` {
        @Test
        fun `returns book when found`() {
            val book = createValidBook()
            repository.save(book)

            val result = repository.findById(book.id)

            assertTrue(result.isRight())
            val foundBook = result.getOrNull()
            assertNotNull(foundBook)
            assertEquals(book.id, foundBook.id)
            assertEquals(book.title, foundBook.title)
        }

        @Test
        fun `returns null when not found`() {
            val nonExistentId = IdScalar.random()

            val result = repository.findById(nonExistentId)

            assertTrue(result.isRight())
            assertNull(result.getOrNull())
        }
    }

    @Nested
    inner class `findByIsbn` {
        @Test
        fun `returns book when found by ISBN-13`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn13))
            repository.save(book)

            val result = repository.findByIsbn(IsbnScalar.fromUnsafe(validIsbn13))

            assertTrue(result.isRight())
            val foundBook = result.getOrNull()
            assertNotNull(foundBook)
            assertEquals(book.id, foundBook.id)
        }

        @Test
        fun `returns book when found by ISBN-10`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn10))
            repository.save(book)

            val result = repository.findByIsbn(IsbnScalar.fromUnsafe(validIsbn10))

            assertTrue(result.isRight())
            val foundBook = result.getOrNull()
            assertNotNull(foundBook)
            assertEquals(book.id, foundBook.id)
        }

        @Test
        fun `returns null when ISBN not found`() {
            val result = repository.findByIsbn(IsbnScalar.fromUnsafe(validIsbn13))

            assertTrue(result.isRight())
            assertNull(result.getOrNull())
        }
    }

    @Nested
    inner class `findByAuthorId` {
        @Test
        fun `returns books by author`() {
            val book1 = createValidBook(
                isbn = IsbnScalar.fromUnsafe(validIsbn13),
                title = "Book One",
            )
            val book2 = createValidBook(
                isbn = IsbnScalar.fromUnsafe(validIsbn10),
                title = "Book Two",
            )
            repository.save(book1)
            repository.save(book2)

            val result = repository.findByAuthorId(testAuthor.id)

            assertTrue(result.isRight())
            val books = result.getOrNull()!!
            assertEquals(2, books.size)
            assertTrue(books.any { it.title == "Book One" })
            assertTrue(books.any { it.title == "Book Two" })
        }

        @Test
        fun `returns empty list when author has no books`() {
            val otherAuthor = createAndSaveAuthor("Other Author")

            val result = repository.findByAuthorId(otherAuthor.id)

            assertTrue(result.isRight())
            assertEquals(emptyList(), result.getOrNull())
        }

        @Test
        fun `only returns books for specific author`() {
            val otherAuthor = createAndSaveAuthor("Other Author")
            val book1 = createValidBook(
                isbn = IsbnScalar.fromUnsafe(validIsbn13),
                title = "Book by Test Author",
            )
            val book2 = createValidBook(
                isbn = IsbnScalar.fromUnsafe(validIsbn10),
                title = "Book by Other Author",
                authorId = otherAuthor.id,
            )
            repository.save(book1)
            repository.save(book2)

            val result = repository.findByAuthorId(testAuthor.id)

            assertTrue(result.isRight())
            val books = result.getOrNull()!!
            assertEquals(1, books.size)
            assertEquals("Book by Test Author", books[0].title)
        }
    }

    @Nested
    inner class `findAll` {
        @Test
        fun `returns empty list when no books exist`() {
            val result = repository.findAll()

            assertTrue(result.isRight())
            assertEquals(emptyList(), result.getOrNull())
        }

        @Test
        fun `returns all books`() {
            val book1 = createValidBook(
                isbn = IsbnScalar.fromUnsafe(validIsbn13),
                title = "Book One",
            )
            val book2 = createValidBook(
                isbn = IsbnScalar.fromUnsafe(validIsbn10),
                title = "Book Two",
            )
            repository.save(book1)
            repository.save(book2)

            val result = repository.findAll()

            assertTrue(result.isRight())
            val books = result.getOrNull()!!
            assertEquals(2, books.size)
            assertTrue(books.any { it.title == "Book One" })
            assertTrue(books.any { it.title == "Book Two" })
        }
    }

    @Nested
    inner class `deleteById` {
        @Test
        fun `returns true when book is deleted`() {
            val book = createValidBook()
            repository.save(book)

            val result = repository.deleteById(book.id)

            assertTrue(result.isRight())
            assertTrue(result.getOrNull()!!)
            assertFalse(springDataRepository.existsById(book.id.value))
        }

        @Test
        fun `returns false when book does not exist`() {
            val nonExistentId = IdScalar.random()

            val result = repository.deleteById(nonExistentId)

            assertTrue(result.isRight())
            assertFalse(result.getOrNull()!!)
        }
    }

    @Nested
    inner class `existsById` {
        @Test
        fun `returns true when book exists`() {
            val book = createValidBook()
            repository.save(book)

            assertTrue(repository.existsById(book.id))
        }

        @Test
        fun `returns false when book does not exist`() {
            assertFalse(repository.existsById(IdScalar.random()))
        }
    }

    @Nested
    inner class `existsByIsbn` {
        @Test
        fun `returns true when ISBN exists`() {
            val book = createValidBook(isbn = IsbnScalar.fromUnsafe(validIsbn13))
            repository.save(book)

            assertTrue(repository.existsByIsbn(IsbnScalar.fromUnsafe(validIsbn13)))
        }

        @Test
        fun `returns false when ISBN does not exist`() {
            assertFalse(repository.existsByIsbn(IsbnScalar.fromUnsafe(validIsbn13)))
        }
    }

    @Nested
    inner class `existsByAuthorId` {
        @Test
        fun `returns true when author has books`() {
            val book = createValidBook()
            repository.save(book)

            assertTrue(repository.existsByAuthorId(testAuthor.id))
        }

        @Test
        fun `returns false when author has no books`() {
            val otherAuthor = createAndSaveAuthor("Other Author")

            assertFalse(repository.existsByAuthorId(otherAuthor.id))
        }
    }

    @Nested
    inner class `round-trip persistence` {
        @Test
        fun `book survives save and retrieve cycle`() {
            val original = createValidBook(
                title = "Domain-Driven Design",
            )

            repository.save(original)
            val result = repository.findById(original.id)

            assertTrue(result.isRight())
            val retrieved = result.getOrNull()
            assertNotNull(retrieved)
            assertEquals(original.id, retrieved.id)
            assertEquals(original.isbn, retrieved.isbn)
            assertEquals(original.title, retrieved.title)
            assertEquals(original.authorId, retrieved.authorId)
        }

        @Test
        fun `preserves special characters in title`() {
            val original = createValidBook(
                title = "C++: A Complete Reference (3rd Ed.)",
            )

            repository.save(original)
            val result = repository.findById(original.id)

            assertTrue(result.isRight())
            val retrieved = result.getOrNull()!!
            assertEquals(original.title, retrieved.title)
        }

        @Test
        fun `preserves maximum length title`() {
            val original = createValidBook(
                title = "A".repeat(500),
            )

            repository.save(original)
            val result = repository.findById(original.id)

            assertTrue(result.isRight())
            val retrieved = result.getOrNull()!!
            assertEquals(500, retrieved.title.length)
        }
    }
}
