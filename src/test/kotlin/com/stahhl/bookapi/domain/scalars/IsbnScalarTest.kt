package com.stahhl.bookapi.domain.scalars

import arrow.core.raise.either
import com.stahhl.bookapi.domain.errors.IsbnError
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class IsbnScalarTest {

    @Nested
    inner class `ISBN-10 validation` {

        @Test
        fun `accepts valid ISBN-10`() {
            // "0306406152" is a valid ISBN-10
            val result = either { IsbnScalar.from("0306406152") }

            assertTrue(result.isRight())
            assertEquals("0306406152", result.getOrNull()?.value)
        }

        @Test
        fun `accepts valid ISBN-10 with X check digit`() {
            // "155404295X" is a valid ISBN-10 with X check digit
            val result = either { IsbnScalar.from("155404295X") }

            assertTrue(result.isRight())
            assertEquals("155404295X", result.getOrNull()?.value)
        }

        @Test
        fun `accepts ISBN-10 with hyphens`() {
            val result = either { IsbnScalar.from("0-306-40615-2") }

            assertTrue(result.isRight())
            assertEquals("0306406152", result.getOrNull()?.value)
        }

        @Test
        fun `accepts ISBN-10 with spaces`() {
            val result = either { IsbnScalar.from("0 306 40615 2") }

            assertTrue(result.isRight())
        }

        @Test
        fun `normalizes lowercase x to uppercase`() {
            val result = either { IsbnScalar.from("155404295x") }

            assertTrue(result.isRight())
            assertEquals("155404295X", result.getOrNull()?.value)
        }

        @Test
        fun `rejects ISBN-10 with non-digit leading characters`() {
            val result = either { IsbnScalar.from("030640615A") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidIsbn10Format>(result.leftOrNull())
        }

        @Test
        fun `rejects ISBN-10 with invalid check digit character`() {
            val result = either { IsbnScalar.from("030640615Y") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidIsbn10Format>(result.leftOrNull())
        }

        @Test
        fun `rejects ISBN-10 with invalid checksum`() {
            val result = either { IsbnScalar.from("0306406153") } // Wrong check digit

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidChecksum>(result.leftOrNull())
            assertEquals("ISBN-10", (result.leftOrNull() as IsbnError.InvalidChecksum).type)
        }
    }

    @Nested
    inner class `ISBN-13 validation` {

        @Test
        fun `accepts valid ISBN-13 starting with 978`() {
            // "9780306406157" is a valid ISBN-13
            val result = either { IsbnScalar.from("9780306406157") }

            assertTrue(result.isRight())
            assertEquals("9780306406157", result.getOrNull()?.value)
        }

        @Test
        fun `accepts valid ISBN-13 starting with 979`() {
            // "9791090636071" is a valid ISBN-13
            val result = either { IsbnScalar.from("9791090636071") }

            assertTrue(result.isRight())
        }

        @Test
        fun `accepts ISBN-13 with hyphens`() {
            val result = either { IsbnScalar.from("978-0-306-40615-7") }

            assertTrue(result.isRight())
            assertEquals("9780306406157", result.getOrNull()?.value)
        }

        @Test
        fun `rejects ISBN-13 with non-digit characters`() {
            val result = either { IsbnScalar.from("978030640615X") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidIsbn13Format>(result.leftOrNull())
        }

        @Test
        fun `rejects ISBN-13 with invalid prefix`() {
            val result = either { IsbnScalar.from("9770306406157") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidIsbn13Format>(result.leftOrNull())
        }

        @Test
        fun `rejects ISBN-13 with invalid checksum`() {
            val result = either { IsbnScalar.from("9780306406158") } // Wrong check digit

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidChecksum>(result.leftOrNull())
            assertEquals("ISBN-13", (result.leftOrNull() as IsbnError.InvalidChecksum).type)
        }
    }

    @Nested
    inner class `invalid length` {

        @Test
        fun `rejects ISBN with wrong length`() {
            val result = either { IsbnScalar.from("12345") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidLength>(result.leftOrNull())
            assertEquals(5, (result.leftOrNull() as IsbnError.InvalidLength).actual)
        }

        @Test
        fun `rejects empty string`() {
            val result = either { IsbnScalar.from("") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidLength>(result.leftOrNull())
        }

        @Test
        fun `rejects 11-character string`() {
            val result = either { IsbnScalar.from("12345678901") }

            assertTrue(result.isLeft())
            assertIs<IsbnError.InvalidLength>(result.leftOrNull())
        }
    }

    @Nested
    inner class `fromEither` {

        @Test
        fun `returns Right for valid ISBN`() {
            val result = IsbnScalar.fromEither("9780306406157")

            assertTrue(result.isRight())
        }

        @Test
        fun `returns Left for invalid ISBN`() {
            val result = IsbnScalar.fromEither("invalid")

            assertTrue(result.isLeft())
        }
    }

    @Nested
    inner class `fromUnsafe` {

        @Test
        fun `creates IsbnScalar from valid ISBN`() {
            val isbn = IsbnScalar.fromUnsafe("9780306406157")

            assertEquals("9780306406157", isbn.value)
        }

        @Test
        fun `throws IllegalArgumentException for invalid ISBN`() {
            val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                IsbnScalar.fromUnsafe("invalid")
            }
            assertTrue(exception.message?.contains("ISBN must be 10 or 13 characters") == true)
        }
    }

    @Nested
    inner class `toString` {

        @Test
        fun `returns ISBN string representation`() {
            val isbn = IsbnScalar.fromUnsafe("9780306406157")

            assertEquals("9780306406157", isbn.toString())
        }
    }
}
