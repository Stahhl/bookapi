package com.stahhl.bookapi.infrastructure.persistence

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.stahhl.bookapi.domain.errors.BookError
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.domain.types.Book
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * JPA implementation of the BookRepository port.
 * Adapts between the domain Book type and the JPA BookEntity.
 */
@Repository
class JpaBookRepository(
    private val springDataRepository: SpringDataBookRepository,
) : BookRepository {

    override fun findById(id: IdScalar): Either<BookError, Book?> {
        val entity = springDataRepository.findByIdOrNull(id.value)
            ?: return null.right()
        return entity.toDomain().map { it }
    }

    override fun findByIsbn(isbn: IsbnScalar): Either<BookError, Book?> {
        val entity = springDataRepository.findByIsbn(isbn.value)
            ?: return null.right()
        return entity.toDomain().map { it }
    }

    override fun findByAuthorId(authorId: IdScalar): Either<BookError, List<Book>> = either {
        val entities = springDataRepository.findByAuthorId(authorId.value)
        entities.map { it.toDomain().bind() }
    }

    override fun findAll(): Either<BookError, List<Book>> = either {
        val entities = springDataRepository.findAll()
        entities.map { it.toDomain().bind() }
    }

    override fun save(book: Book): Either<BookError, Book> {
        val entity = BookEntity.from(book)
        val saved = springDataRepository.save(entity)
        return saved.toDomain()
    }

    override fun deleteById(id: IdScalar): Either<BookError, Boolean> {
        return if (springDataRepository.existsById(id.value)) {
            springDataRepository.deleteById(id.value)
            true.right()
        } else {
            false.right()
        }
    }

    override fun existsById(id: IdScalar): Boolean =
        springDataRepository.existsById(id.value)

    override fun existsByIsbn(isbn: IsbnScalar): Boolean =
        springDataRepository.existsByIsbn(isbn.value)

    override fun existsByAuthorId(authorId: IdScalar): Boolean =
        springDataRepository.existsByAuthorId(authorId.value)
}
