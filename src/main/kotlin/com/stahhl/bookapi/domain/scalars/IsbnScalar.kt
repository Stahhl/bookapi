package com.stahhl.bookapi.domain.scalars

@JvmInline
value class IsbnScalar private constructor(
    val value: String,
) {
    companion object {
        fun from(value: String): IsbnScalar {
            val normalized = value.replace("-", "").replace(" ", "")

            return when (normalized.length) {
                10 -> parseIsbn10(normalized)
                13 -> parseIsbn13(normalized)
                else -> throw IllegalArgumentException(
                    "ISBN must be 10 or 13 characters (got ${normalized.length})"
                )
            }
        }

        private fun parseIsbn10(isbn: String): IsbnScalar {
            // ISBN-10: first 9 chars must be digits, last can be digit or 'X'
            val digits = isbn.substring(0, 9)
            val checkChar = isbn[9].uppercaseChar()

            if (!digits.all { it.isDigit() }) {
                throw IllegalArgumentException("ISBN-10 must have 9 leading digits")
            }

            if (!checkChar.isDigit() && checkChar != 'X') {
                throw IllegalArgumentException("ISBN-10 check digit must be 0-9 or X")
            }

            val checkValue = if (checkChar == 'X') 10 else checkChar.digitToInt()
            val sum = digits.mapIndexed { index, c ->
                c.digitToInt() * (10 - index)
            }.sum() + checkValue

            if (sum % 11 != 0) {
                throw IllegalArgumentException("Invalid ISBN-10 checksum")
            }

            return IsbnScalar(isbn.uppercase())
        }

        private fun parseIsbn13(isbn: String): IsbnScalar {
            // ISBN-13: all 13 chars must be digits, must start with 978 or 979
            if (!isbn.all { it.isDigit() }) {
                throw IllegalArgumentException("ISBN-13 must contain only digits")
            }

            if (!isbn.startsWith("978") && !isbn.startsWith("979")) {
                throw IllegalArgumentException("ISBN-13 must start with 978 or 979")
            }

            val sum = isbn.mapIndexed { index, c ->
                val digit = c.digitToInt()
                if (index % 2 == 0) digit else digit * 3
            }.sum()

            if (sum % 10 != 0) {
                throw IllegalArgumentException("Invalid ISBN-13 checksum")
            }

            return IsbnScalar(isbn)
        }
    }
}
