package com.stahhl.bookapi.domain.scalars

import arrow.core.raise.either
import com.stahhl.bookapi.domain.errors.IdError
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class IdScalarTest {

    @Nested
    inner class `from with Raise context` {

        @Test
        fun `creates IdScalar from valid UUID string`() {
            val uuid = UUID.randomUUID()
            val result = either { IdScalar.from(uuid.toString()) }

            assertTrue(result.isRight())
            assertEquals(uuid, result.getOrNull()?.value)
        }

        @Test
        fun `fails with InvalidFormat for malformed UUID`() {
            val result = either { IdScalar.from("not-a-uuid") }

            assertTrue(result.isLeft())
            assertIs<IdError.InvalidFormat>(result.leftOrNull())
            assertEquals("not-a-uuid", (result.leftOrNull() as IdError.InvalidFormat).input)
        }

        @Test
        fun `fails with InvalidFormat for empty string`() {
            val result = either { IdScalar.from("") }

            assertTrue(result.isLeft())
            assertIs<IdError.InvalidFormat>(result.leftOrNull())
        }
    }

    @Nested
    inner class `fromEither` {

        @Test
        fun `returns Right for valid UUID`() {
            val uuid = UUID.randomUUID()
            val result = IdScalar.fromEither(uuid.toString())

            assertTrue(result.isRight())
            assertEquals(uuid, result.getOrNull()?.value)
        }

        @Test
        fun `returns Left for invalid UUID`() {
            val result = IdScalar.fromEither("invalid")

            assertTrue(result.isLeft())
            assertIs<IdError.InvalidFormat>(result.leftOrNull())
        }
    }

    @Nested
    inner class `random` {

        @Test
        fun `creates unique IdScalar each time`() {
            val id1 = IdScalar.random()
            val id2 = IdScalar.random()

            assertTrue(id1.value != id2.value)
        }
    }

    @Nested
    inner class `fromUnsafe` {

        @Test
        fun `creates IdScalar from valid UUID`() {
            val uuid = UUID.randomUUID()
            val id = IdScalar.fromUnsafe(uuid.toString())

            assertEquals(uuid, id.value)
        }

        @Test
        fun `throws IllegalArgumentException for invalid UUID`() {
            val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                IdScalar.fromUnsafe("invalid")
            }
            assertTrue(exception.message?.contains("Invalid ID format") == true)
        }
    }

    @Nested
    inner class `toString` {

        @Test
        fun `returns UUID string representation`() {
            val uuid = UUID.randomUUID()
            val id = IdScalar.fromUnsafe(uuid.toString())

            assertEquals(uuid.toString(), id.toString())
        }
    }
}
