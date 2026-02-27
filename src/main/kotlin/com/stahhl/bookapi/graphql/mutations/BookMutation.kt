package com.stahhl.bookapi.graphql.mutations

import arrow.core.getOrElse
import com.expediagroup.graphql.server.operations.Mutation
import com.stahhl.bookapi.domain.repositories.AuthorRepository
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.repositories.CoverUploadRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book
import com.stahhl.bookapi.domain.types.BookCover
import com.stahhl.bookapi.graphql.types.BookType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * GraphQL Mutation resolver for Book operations.
 */
@Component
class BookMutation(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val coverUploadRepository: CoverUploadRepository,
) : Mutation {
    /**
     * Create a new book.
     *
     * @param isbn The ISBN of the book (ISBN-10 or ISBN-13)
     * @param title The title of the book
     * @param authorId The ID of the author
     * @return The created book
     */
    fun createBook(
        isbn: IsbnScalar,
        title: String,
        authorId: IdScalar,
    ): BookType {
        // Validate author exists
        val author = authorRepository.findById(authorId)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }
            ?: throw IllegalArgumentException("Author not found with id: $authorId")

        // Create the book domain object with validation
        val book = Book.createNewEither(isbn, title, authorId)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }

        // Save and return
        return bookRepository.save(book)
            .map { BookType(it) }
            .getOrElse { error -> throw IllegalStateException(error.message) }
    }

    /**
     * Update an existing book.
     *
     * @param id The ID of the book to update
     * @param isbn Optional new ISBN
     * @param title Optional new title
     * @param authorId Optional new author ID
     * @return The updated book
     */
    fun updateBook(
        id: IdScalar,
        isbn: IsbnScalar? = null,
        title: String? = null,
        authorId: IdScalar? = null,
    ): BookType {
        // Find existing book
        val existingBook = bookRepository.findById(id)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }
            ?: throw IllegalArgumentException("Book not found with id: $id")

        // If authorId is being changed, validate new author exists
        if (authorId != null && authorId != existingBook.authorId) {
            val author = authorRepository.findById(authorId)
                .getOrElse { error -> throw IllegalArgumentException(error.message) }
                ?: throw IllegalArgumentException("Author not found with id: $authorId")
        }

        // Update the book with provided fields
        val updatedBook = existingBook.updateEither(
            title = title ?: existingBook.title,
            authorId = authorId ?: existingBook.authorId,
            isbn = isbn ?: existingBook.isbn,
        ).getOrElse { error -> throw IllegalArgumentException(error.message) }

        // Save and return
        return bookRepository.save(updatedBook)
            .map { BookType(it) }
            .getOrElse { error -> throw IllegalStateException(error.message) }
    }

    /**
     * Delete a book by its ID.
     *
     * @param id The ID of the book to delete
     * @return True if the book was deleted, false if it didn't exist
     */
    fun deleteBook(id: IdScalar): Boolean =
        bookRepository.deleteById(id)
            .getOrElse { error -> throw IllegalStateException(error.message) }

    /**
     * Attaches a staged cover upload to a book and stores the cover description.
     */
    @Transactional
    fun attachBookCover(
        bookId: IdScalar,
        uploadId: IdScalar,
        description: String,
    ): BookType {
        val existingBook = bookRepository.findById(bookId)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }
            ?: throw IllegalArgumentException("Book not found with id: $bookId")

        val upload = coverUploadRepository.findById(uploadId)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }
            ?: throw IllegalArgumentException("Cover upload not found with id: $uploadId")

        if (upload.isExpired(Instant.now())) {
            throw IllegalArgumentException("Cover upload has expired: $uploadId")
        }
        if (upload.isConsumed()) {
            throw IllegalArgumentException("Cover upload has already been consumed: $uploadId")
        }

        val cover = BookCover.createEither(
            storagePath = upload.storagePath,
            contentType = upload.contentType,
            description = description,
        ).getOrElse { error -> throw IllegalArgumentException(error.message) }

        val updatedBook = existingBook.updateEither(cover = cover)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }

        val savedBook = bookRepository.save(updatedBook)
            .getOrElse { error -> throw IllegalStateException(error.message) }

        val consumedUpload = upload.consumeEither()
            .getOrElse { error -> throw IllegalArgumentException(error.message) }
        coverUploadRepository.save(consumedUpload)
            .getOrElse { error -> throw IllegalStateException(error.message) }

        return BookType(savedBook)
    }
}
