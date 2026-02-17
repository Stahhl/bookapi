package com.stahhl.bookapi.domain.scalars

import java.util.UUID

@JvmInline
value class IdScalar private constructor(
    val value: UUID,
) {
    companion object {
        fun from(value: String): IdScalar = IdScalar(UUID.fromString(value))

        fun random(): IdScalar = IdScalar(UUID.randomUUID())
    }
}
