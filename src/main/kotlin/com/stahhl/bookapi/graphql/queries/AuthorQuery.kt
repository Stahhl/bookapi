package com.stahhl.bookapi.graphql.queries

import arrow.core.getOrElse
import com.expediagroup.graphql.server.operations.Query
import com.stahhl.bookapi.domain.repositories.AuthorRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.graphql.types.AuthorType
import org.springframework.stereotype.Component

/**
 * GraphQL Query resolver for Author operations.
 */
@Component
class AuthorQuery(
    private val authorRepository: AuthorRepository,
) : Query {
    /**
     * Retrieve an author by their unique identifier.
     */
    fun author(id: IdScalar): AuthorType? =
        authorRepository
            .findById(id)
            .getOrElse { null }
            ?.let { AuthorType(it) }

    /**
     * Retrieve an author by their name.
     */
    fun authorByName(name: String): AuthorType? =
        authorRepository
            .findByName(name)
            .getOrElse { null }
            ?.let { AuthorType(it) }

    /**
     * Retrieve all authors.
     */
    fun authors(): List<AuthorType> =
        authorRepository
            .findAll()
            .getOrElse { emptyList() }
            .map { AuthorType(it) }
}
