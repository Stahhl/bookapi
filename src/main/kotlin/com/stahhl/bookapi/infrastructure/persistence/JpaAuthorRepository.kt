package com.stahhl.bookapi.infrastructure.persistence

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.stahhl.bookapi.domain.errors.AuthorError
import com.stahhl.bookapi.domain.repositories.AuthorRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.Author
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * JPA implementation of the AuthorRepository port.
 * Adapts between the domain Author type and the JPA AuthorEntity.
 */
@Repository
class JpaAuthorRepository(
    private val springDataRepository: SpringDataAuthorRepository,
) : AuthorRepository {

    override fun findById(id: IdScalar): Either<AuthorError, Author?> {
        val entity = springDataRepository.findByIdOrNull(id.value)
            ?: return null.right()
        return entity.toDomain().map { it }
    }

    override fun findByName(name: String): Either<AuthorError, Author?> {
        val entity = springDataRepository.findByName(name)
            ?: return null.right()
        return entity.toDomain().map { it }
    }

    override fun findAll(): Either<AuthorError, List<Author>> = either {
        val entities = springDataRepository.findAll()
        entities.map { it.toDomain().bind() }
    }

    override fun save(author: Author): Either<AuthorError, Author> {
        val entity = AuthorEntity.from(author)
        val saved = springDataRepository.save(entity)
        return saved.toDomain()
    }

    override fun deleteById(id: IdScalar): Either<AuthorError, Boolean> {
        return if (springDataRepository.existsById(id.value)) {
            springDataRepository.deleteById(id.value)
            true.right()
        } else {
            false.right()
        }
    }

    override fun existsById(id: IdScalar): Boolean =
        springDataRepository.existsById(id.value)

    override fun existsByName(name: String): Boolean =
        springDataRepository.existsByName(name)
}
