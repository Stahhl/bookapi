package com.stahhl.bookapi.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA repository for BookEntity.
 * This is an internal infrastructure detail - external code should use BookRepository.
 */
interface SpringDataBookRepository : JpaRepository<BookEntity, UUID> {
    fun findByIsbn(isbn: String): BookEntity?

    fun findByAuthorId(authorId: UUID): List<BookEntity>

    fun existsByIsbn(isbn: String): Boolean

    fun existsByAuthorId(authorId: UUID): Boolean
}
