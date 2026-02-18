package com.stahhl.bookapi.domain.scalars

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.stahhl.bookapi.domain.errors.IsbnError

@JvmInline
value class IsbnScalar private constructor(
    val value: String,
) {
    override fun toString(): String = value

    companion object {
        /**
         * Creates an IsbnScalar from a string using the Raise DSL.
         * This is the idiomatic Arrow way for use in functional pipelines.
         */
        context(Raise<IsbnError>)
        fun from(value: String): IsbnScalar {
            val normalized = value.replace("-", "").replace(" ", "")

            return when (normalized.length) {
                10 -> parseIsbn10(normalized)
                13 -> parseIsbn13(normalized)
                else -> raise(IsbnError.InvalidLength(normalized.length))
            }
        }

        /**
         * Creates an IsbnScalar from a string, returning Either for explicit error handling.
         * Useful when you need to handle the result outside of a Raise context.
         */
        fun fromEither(value: String): Either<IsbnError, IsbnScalar> = either { from(value) }

        /**
         * Unsafe variant that throws on invalid input.
         * Only use when you're certain the input is valid or in legacy code paths.
         */
        fun fromUnsafe(value: String): IsbnScalar =
            fromEither(value).getOrElse { error ->
                throw IllegalArgumentException(error.message)
            }

        context(Raise<IsbnError>)
        private fun parseIsbn10(isbn: String): IsbnScalar {
            val digits = isbn.substring(0, 9)
            val checkChar = isbn[9].uppercaseChar()

            ensure(digits.all { it.isDigit() }) {
                IsbnError.InvalidIsbn10Format("must have 9 leading digits")
            }

            ensure(checkChar.isDigit() || checkChar == 'X') {
                IsbnError.InvalidIsbn10Format("check digit must be 0-9 or X")
            }

            val checkValue = if (checkChar == 'X') 10 else checkChar.digitToInt()
            val sum = digits.mapIndexed { index, c ->
                c.digitToInt() * (10 - index)
            }.sum() + checkValue

            ensure(sum % 11 == 0) {
                IsbnError.InvalidChecksum("ISBN-10")
            }

            return IsbnScalar(isbn.uppercase())
        }

        context(Raise<IsbnError>)
        private fun parseIsbn13(isbn: String): IsbnScalar {
            ensure(isbn.all { it.isDigit() }) {
                IsbnError.InvalidIsbn13Format("must contain only digits")
            }

            ensure(isbn.startsWith("978") || isbn.startsWith("979")) {
                IsbnError.InvalidIsbn13Format("must start with 978 or 979")
            }

            val sum = isbn.mapIndexed { index, c ->
                val digit = c.digitToInt()
                if (index % 2 == 0) digit else digit * 3
            }.sum()

            ensure(sum % 10 == 0) {
                IsbnError.InvalidChecksum("ISBN-13")
            }

            return IsbnScalar(isbn)
        }
    }
}
