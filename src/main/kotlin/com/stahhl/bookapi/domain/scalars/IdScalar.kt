package com.stahhl.bookapi.domain.scalars

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import com.stahhl.bookapi.domain.errors.IdError
import java.util.UUID

@JvmInline
value class IdScalar private constructor(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        /**
         * Creates an IdScalar from a string using the Raise DSL.
         * This is the idiomatic Arrow way for use in functional pipelines.
         */
        context(Raise<IdError>)
        fun from(value: String): IdScalar = catch(
            block = { IdScalar(UUID.fromString(value)) },
            catch = { e: IllegalArgumentException ->
                raise(IdError.InvalidFormat(value, e.message ?: "Invalid UUID format"))
            }
        )

        /**
         * Creates an IdScalar from a string, returning Either for explicit error handling.
         * Useful when you need to handle the result outside of a Raise context.
         */
        fun fromEither(value: String): Either<IdError, IdScalar> = either { from(value) }

        /**
         * Creates a random IdScalar. This operation cannot fail.
         */
        fun random(): IdScalar = IdScalar(UUID.randomUUID())

        /**
         * Unsafe variant that throws on invalid input.
         * Only use when you're certain the input is valid or in legacy code paths.
         */
        fun fromUnsafe(value: String): IdScalar = 
            fromEither(value).getOrElse { error -> 
                throw IllegalArgumentException(error.message) 
            }

        /**
         * Creates an IdScalar directly from a UUID.
         * This operation cannot fail since UUID is already valid.
         */
        fun fromUUID(value: UUID): IdScalar = IdScalar(value)
    }
}
