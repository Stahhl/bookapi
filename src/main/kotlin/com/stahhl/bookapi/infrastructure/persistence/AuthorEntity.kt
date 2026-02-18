package com.stahhl.bookapi.infrastructure.persistence

import arrow.core.Either
import arrow.core.raise.either
import com.stahhl.bookapi.domain.errors.AuthorError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.Author
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * JPA entity for persisting authors.
 * This is an infrastructure concern - the domain Author type remains pure and annotation-free.
 */
@Entity
@Table(name = "authors")
class AuthorEntity(
    @Id
    val id: UUID,

    @Column(nullable = false, length = 200)
    val name: String,
) {
    /**
     * JPA requires a no-arg constructor for entity hydration.
     */
    protected constructor() : this(
        id = UUID.randomUUID(),
        name = "",
    )

    /**
     * Converts this entity to a domain Author.
     * Returns Either to handle potential validation errors during hydration.
     */
    fun toDomain(): Either<AuthorError, Author> = either {
        Author.create(
            id = IdScalar.fromUUID(id),
            name = name,
        )
    }

    companion object {
        /**
         * Creates an AuthorEntity from a domain Author.
         * This conversion cannot fail since Author is already validated.
         */
        fun from(author: Author): AuthorEntity = AuthorEntity(
            id = author.id.value,
            name = author.name,
        )
    }
}
