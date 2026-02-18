package com.stahhl.bookapi.graphql.types

import arrow.core.getOrElse
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.Author
import org.springframework.beans.factory.annotation.Autowired

data class AuthorType(
    private val author: Author,
) {
    val id: IdScalar = author.id
    val name: String = author.name

    fun books(
        @GraphQLIgnore @Autowired bookRepository: BookRepository,
    ): List<BookType> =
        bookRepository
            .findByAuthorId(author.id)
            .getOrElse { emptyList() }
            .map { BookType(it) }
}
