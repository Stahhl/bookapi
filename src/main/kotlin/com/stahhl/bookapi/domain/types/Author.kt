package com.stahhl.bookapi.domain.types

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.stahhl.bookapi.domain.errors.AuthorError
import com.stahhl.bookapi.domain.scalars.IdScalar

/**
 * Author domain type with validated invariants.
 * Uses a private constructor to ensure all instances are valid.
 * An author can have many books (one-to-many relationship).
 */
data class Author private constructor(
    val id: IdScalar,
    val name: String,
) {
    companion object {
        /**
         * Creates an Author using the Raise DSL with accumulated validation errors.
         * All validation errors are collected rather than failing on the first one.
         */
        context(Raise<AuthorError>)
        fun create(
            id: IdScalar,
            name: String,
        ): Author {
            val errors = mutableListOf<String>()

            if (name.isBlank()) {
                errors.add("Name cannot be blank")
            } else if (name.length > 200) {
                errors.add("Name cannot exceed 200 characters")
            }

            ensure(errors.isEmpty()) {
                AuthorError.ValidationFailed(errors)
            }

            return Author(
                id = id,
                name = name.trim(),
            )
        }

        /**
         * Creates an Author returning Either for explicit error handling.
         */
        fun createEither(
            id: IdScalar,
            name: String,
        ): Either<AuthorError, Author> = either { create(id, name) }

        /**
         * Creates a new Author with a random ID.
         */
        context(Raise<AuthorError>)
        fun createNew(
            name: String,
        ): Author =
            create(
                id = IdScalar.random(),
                name = name,
            )

        /**
         * Creates a new Author with a random ID, returning Either.
         */
        fun createNewEither(
            name: String,
        ): Either<AuthorError, Author> = either { createNew(name) }
    }

    /**
     * Creates a copy with updated fields, re-validating the result.
     */
    context(Raise<AuthorError>)
    fun update(
        name: String = this.name,
    ): Author =
        create(
            id = this.id,
            name = name,
        )

    /**
     * Creates a copy with updated fields, returning Either.
     */
    fun updateEither(
        name: String = this.name,
    ): Either<AuthorError, Author> = either { update(name) }
}
