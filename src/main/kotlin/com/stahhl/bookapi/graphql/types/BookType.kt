package com.stahhl.bookapi.graphql.types

import arrow.core.getOrElse
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.stahhl.bookapi.domain.repositories.AuthorRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book
import org.springframework.beans.factory.annotation.Autowired

/**
 * GraphQL representation of a Book.
 * Maps from domain Book without exposing Arrow types or domain-specific methods.
 */
data class BookType(
    private val book: Book,
) {
    val id: IdScalar = book.id
    val title: String = book.title
    val isbn: IsbnScalar = book.isbn

    fun author(
        @GraphQLIgnore @Autowired authorRepository: AuthorRepository,
    ): AuthorType {
        val author =
            authorRepository
                .findById(book.authorId)
                .getOrElse { null }
                ?: throw IllegalStateException("Author not found for ID: ${book.authorId}")
        return AuthorType(author)
    }
}
