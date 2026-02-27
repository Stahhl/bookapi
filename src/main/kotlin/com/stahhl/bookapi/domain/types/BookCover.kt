package com.stahhl.bookapi.domain.types

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.stahhl.bookapi.domain.errors.BookError

/**
 * Cover metadata for a book.
 * Holds storage location and client-facing metadata, but not the binary itself.
 */
data class BookCover private constructor(
    val storagePath: String,
    val contentType: String,
    val description: String,
) {
    companion object {
        context(Raise<BookError>)
        fun create(
            storagePath: String,
            contentType: String,
            description: String,
        ): BookCover {
            val errors = mutableListOf<String>()

            if (storagePath.isBlank()) {
                errors.add("Cover storage path cannot be blank")
            }
            if (contentType.isBlank()) {
                errors.add("Cover content type cannot be blank")
            } else if (!contentType.startsWith("image/")) {
                errors.add("Cover content type must be an image/* type")
            }
            if (description.isBlank()) {
                errors.add("Cover description cannot be blank")
            } else if (description.length > 1000) {
                errors.add("Cover description cannot exceed 1000 characters")
            }

            ensure(errors.isEmpty()) {
                BookError.ValidationFailed(errors)
            }

            return BookCover(
                storagePath = storagePath.trim(),
                contentType = contentType.trim().lowercase(),
                description = description.trim(),
            )
        }

        fun createEither(
            storagePath: String,
            contentType: String,
            description: String,
        ): Either<BookError, BookCover> = either { create(storagePath, contentType, description) }
    }
}
