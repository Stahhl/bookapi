package com.stahhl.bookapi.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA repository for AuthorEntity.
 * This is an internal infrastructure detail - external code should use AuthorRepository.
 */
interface SpringDataAuthorRepository : JpaRepository<AuthorEntity, UUID> {
    fun findByName(name: String): AuthorEntity?

    fun existsByName(name: String): Boolean
}
