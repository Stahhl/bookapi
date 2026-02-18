package com.stahhl.bookapi.domain.repositories

import arrow.core.Either
import com.stahhl.bookapi.domain.errors.AuthorError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.Author

/**
 * Repository interface for Author persistence operations.
 * This is a domain port - implementations live in the infrastructure layer.
 */
interface AuthorRepository {

    /**
     * Finds an author by their ID.
     * Returns Right(Author) if found, Right(null) if not found,
     * or Left(AuthorError) for errors.
     */
    fun findById(id: IdScalar): Either<AuthorError, Author?>

    /**
     * Finds an author by their name.
     * Returns Right(Author) if found, Right(null) if not found,
     * or Left(AuthorError) for errors.
     */
    fun findByName(name: String): Either<AuthorError, Author?>

    /**
     * Retrieves all authors.
     * Returns Left(AuthorError) if any author fails to hydrate.
     */
    fun findAll(): Either<AuthorError, List<Author>>

    /**
     * Saves an author (insert or update).
     * Returns the saved author or an error.
     */
    fun save(author: Author): Either<AuthorError, Author>

    /**
     * Deletes an author by their ID.
     * Returns Right(true) if deleted, Right(false) if not found,
     * or Left(AuthorError) for errors.
     */
    fun deleteById(id: IdScalar): Either<AuthorError, Boolean>

    /**
     * Checks if an author exists with the given ID.
     */
    fun existsById(id: IdScalar): Boolean

    /**
     * Checks if an author exists with the given name.
     */
    fun existsByName(name: String): Boolean
}
