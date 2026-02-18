package com.stahhl.bookapi.domain.types

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.stahhl.bookapi.domain.errors.BookError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar

/**
 * Book domain type with validated invariants.
 * Uses a private constructor to ensure all instances are valid.
 * A book belongs to one author (many-to-one relationship).
 */
data class Book private constructor(
    val id: IdScalar,
    val isbn: IsbnScalar,
    val title: String,
    val authorId: IdScalar,
) {
    companion object {
        /**
         * Creates a Book using the Raise DSL with accumulated validation errors.
         * All validation errors are collected rather than failing on the first one.
         */
        context(Raise<BookError>)
        fun create(
            id: IdScalar,
            isbn: IsbnScalar,
            title: String,
            authorId: IdScalar,
        ): Book {
            val errors = mutableListOf<String>()

            if (title.isBlank()) {
                errors.add("Title cannot be blank")
            } else if (title.length > 500) {
                errors.add("Title cannot exceed 500 characters")
            }

            ensure(errors.isEmpty()) {
                BookError.ValidationFailed(errors)
            }

            return Book(
                id = id,
                isbn = isbn,
                title = title.trim(),
                authorId = authorId,
            )
        }

        /**
         * Creates a Book returning Either for explicit error handling.
         */
        fun createEither(
            id: IdScalar,
            isbn: IsbnScalar,
            title: String,
            authorId: IdScalar,
        ): Either<BookError, Book> = either { create(id, isbn, title, authorId) }

        /**
         * Creates a new Book with a random ID.
         */
        context(Raise<BookError>)
        fun createNew(
            isbn: IsbnScalar,
            title: String,
            authorId: IdScalar,
        ): Book =
            create(
                id = IdScalar.random(),
                isbn = isbn,
                title = title,
                authorId = authorId,
            )

        /**
         * Creates a new Book with a random ID, returning Either.
         */
        fun createNewEither(
            isbn: IsbnScalar,
            title: String,
            authorId: IdScalar,
        ): Either<BookError, Book> = either { createNew(isbn, title, authorId) }
    }

    /**
     * Creates a copy with updated fields, re-validating the result.
     */
    context(Raise<BookError>)
    fun update(
        title: String = this.title,
        authorId: IdScalar = this.authorId,
        isbn: IsbnScalar = this.isbn,
    ): Book =
        create(
            id = this.id,
            isbn = isbn,
            title = title,
            authorId = authorId,
        )

    /**
     * Creates a copy with updated fields, returning Either.
     */
    fun updateEither(
        title: String = this.title,
        authorId: IdScalar = this.authorId,
        isbn: IsbnScalar = this.isbn,
    ): Either<BookError, Book> = either { update(title, authorId, isbn) }
}
