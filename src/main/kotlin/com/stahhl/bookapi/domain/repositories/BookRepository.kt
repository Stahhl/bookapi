package com.stahhl.bookapi.domain.repositories

import arrow.core.Either
import com.stahhl.bookapi.domain.errors.BookError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book

/**
 * Repository interface for Book persistence operations.
 * This is a domain port - implementations live in the infrastructure layer.
 */
interface BookRepository {

    /**
     * Finds a book by its ID.
     * Returns Right(Book) if found, Left(BookError.NotFound) if not found,
     * or Left(BookError) for other errors (e.g., data corruption).
     */
    fun findById(id: IdScalar): Either<BookError, Book?>

    /**
     * Finds a book by its ISBN.
     * Returns Right(Book) if found, Right(null) if not found,
     * or Left(BookError) for errors.
     */
    fun findByIsbn(isbn: IsbnScalar): Either<BookError, Book?>

    /**
     * Finds all books by a specific author.
     * Returns Left(BookError) if any book fails to hydrate.
     */
    fun findByAuthorId(authorId: IdScalar): Either<BookError, List<Book>>

    /**
     * Retrieves all books.
     * Returns Left(BookError) if any book fails to hydrate.
     */
    fun findAll(): Either<BookError, List<Book>>

    /**
     * Saves a book (insert or update).
     * Returns the saved book or an error.
     */
    fun save(book: Book): Either<BookError, Book>

    /**
     * Deletes a book by its ID.
     * Returns Right(true) if deleted, Right(false) if not found,
     * or Left(BookError) for errors.
     */
    fun deleteById(id: IdScalar): Either<BookError, Boolean>

    /**
     * Checks if a book exists with the given ID.
     */
    fun existsById(id: IdScalar): Boolean

    /**
     * Checks if a book exists with the given ISBN.
     */
    fun existsByIsbn(isbn: IsbnScalar): Boolean

    /**
     * Checks if any books exist for the given author.
     */
    fun existsByAuthorId(authorId: IdScalar): Boolean
}
