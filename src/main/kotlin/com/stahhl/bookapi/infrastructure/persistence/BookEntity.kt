package com.stahhl.bookapi.infrastructure.persistence

import arrow.core.Either
import arrow.core.raise.either
import com.stahhl.bookapi.domain.errors.BookError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

/**
 * JPA entity for persisting books.
 * This is an infrastructure concern - the domain Book type remains pure and annotation-free.
 */
@Entity
@Table(name = "books")
class BookEntity(
    @Id
    val id: UUID,

    @Column(nullable = false, unique = true, length = 13)
    val isbn: String,

    @Column(nullable = false, length = 500)
    val title: String,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    val author: AuthorEntity? = null,
) {
    /**
     * JPA requires a no-arg constructor for entity hydration.
     */
    protected constructor() : this(
        id = UUID.randomUUID(),
        isbn = "",
        title = "",
        authorId = UUID.randomUUID(),
    )

    /**
     * Converts this entity to a domain Book.
     * Returns Either to handle potential validation errors during hydration.
     */
    fun toDomain(): Either<BookError, Book> = either {
        val isbnResult = IsbnScalar.fromEither(isbn)
            .mapLeft { BookError.InvalidData("isbn", it.message) }
            .bind()

        Book.create(
            id = IdScalar.fromUUID(id),
            isbn = isbnResult,
            title = title,
            authorId = IdScalar.fromUUID(authorId),
        )
    }

    companion object {
        /**
         * Creates a BookEntity from a domain Book.
         * This conversion cannot fail since Book is already validated.
         */
        fun from(book: Book): BookEntity = BookEntity(
            id = book.id.value,
            isbn = book.isbn.value,
            title = book.title,
            authorId = book.authorId.value,
        )
    }
}
