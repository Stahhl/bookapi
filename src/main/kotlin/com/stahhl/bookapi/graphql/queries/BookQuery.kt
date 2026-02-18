package com.stahhl.bookapi.graphql.queries

import arrow.core.getOrElse
import com.expediagroup.graphql.server.operations.Query
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.graphql.types.BookType
import org.springframework.stereotype.Component

/**
 * GraphQL Query resolver for Book operations.
 */
@Component
class BookQuery(
    private val bookRepository: BookRepository,
) : Query {
    /**
     * Retrieve a book by its unique identifier.
     */
    fun book(id: IdScalar): BookType? =
        bookRepository
            .findById(id)
            .getOrElse { null }
            ?.let { BookType(it) }

    /**
     * Retrieve a book by its ISBN.
     */
    fun bookByIsbn(isbn: IsbnScalar): BookType? =
        bookRepository
            .findByIsbn(isbn)
            .getOrElse { null }
            ?.let { BookType(it) }

    /**
     * Retrieve all books.
     */
    fun books(): List<BookType> =
        bookRepository
            .findAll()
            .getOrElse { emptyList() }
            .map { BookType(it) }
}
