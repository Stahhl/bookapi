package com.stahhl.bookapi.domain.errors

/**
 * Base sealed interface for all domain errors.
 * Using sealed interfaces allows exhaustive when expressions and type-safe error handling.
 */
sealed interface DomainError {
    val message: String
}

/**
 * Errors related to parsing and validation operations.
 */
sealed interface ParseError : DomainError

/**
 * Errors related to ID parsing.
 */
sealed interface IdError : ParseError {
    data class InvalidFormat(val input: String, val cause: String) : IdError {
        override val message: String = "Invalid ID format '$input': $cause"
    }
}

/**
 * Errors related to ISBN parsing and validation.
 */
sealed interface IsbnError : ParseError {
    data class InvalidLength(val actual: Int) : IsbnError {
        override val message: String = "ISBN must be 10 or 13 characters (got $actual)"
    }

    data class InvalidIsbn10Format(val reason: String) : IsbnError {
        override val message: String = "Invalid ISBN-10: $reason"
    }

    data class InvalidIsbn13Format(val reason: String) : IsbnError {
        override val message: String = "Invalid ISBN-13: $reason"
    }

    data class InvalidChecksum(val type: String) : IsbnError {
        override val message: String = "Invalid $type checksum"
    }
}

/**
 * Errors related to Author operations.
 */
sealed interface AuthorError : DomainError {
    data class NotFound(val id: String) : AuthorError {
        override val message: String = "Author not found with id: $id"
    }

    data class ValidationFailed(val errors: List<String>) : AuthorError {
        override val message: String = "Author validation failed: ${errors.joinToString(", ")}"
    }

    data class InvalidData(val field: String, val reason: String) : AuthorError {
        override val message: String = "Invalid author data for field '$field': $reason"
    }
}

/**
 * Errors related to Book operations.
 */
sealed interface BookError : DomainError {
    data class NotFound(val id: String) : BookError {
        override val message: String = "Book not found with id: $id"
    }

    data class ValidationFailed(val errors: List<String>) : BookError {
        override val message: String = "Book validation failed: ${errors.joinToString(", ")}"
    }

    data class InvalidData(val field: String, val reason: String) : BookError {
        override val message: String = "Invalid book data for field '$field': $reason"
    }

    data class AuthorNotFound(val authorId: String) : BookError {
        override val message: String = "Author not found for book with author id: $authorId"
    }
}

/**
 * Errors related to cover upload operations.
 */
sealed interface CoverUploadError : DomainError {
    data class NotFound(val id: String) : CoverUploadError {
        override val message: String = "Cover upload not found with id: $id"
    }

    data class ValidationFailed(val errors: List<String>) : CoverUploadError {
        override val message: String = "Cover upload validation failed: ${errors.joinToString(", ")}"
    }

    data class InvalidData(val field: String, val reason: String) : CoverUploadError {
        override val message: String = "Invalid cover upload data for field '$field': $reason"
    }

    data class Expired(val id: String) : CoverUploadError {
        override val message: String = "Cover upload has expired: $id"
    }

    data class AlreadyConsumed(val id: String) : CoverUploadError {
        override val message: String = "Cover upload has already been consumed: $id"
    }
}
