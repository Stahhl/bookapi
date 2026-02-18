package com.stahhl.bookapi.graphql.mutations

import arrow.core.getOrElse
import com.expediagroup.graphql.server.operations.Mutation
import com.stahhl.bookapi.domain.repositories.AuthorRepository
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.Author
import com.stahhl.bookapi.graphql.types.AuthorType
import org.springframework.stereotype.Component

/**
 * GraphQL Mutation resolver for Author operations.
 */
@Component
class AuthorMutation(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) : Mutation {
    /**
     * Create a new author.
     *
     * @param name The name of the author
     * @return The created author
     */
    fun createAuthor(name: String): AuthorType {
        // Create the author domain object with validation
        val author = Author.createNewEither(name)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }

        // Save and return
        return authorRepository.save(author)
            .map { AuthorType(it) }
            .getOrElse { error -> throw IllegalStateException(error.message) }
    }

    /**
     * Update an existing author.
     *
     * @param id The ID of the author to update
     * @param name Optional new name
     * @return The updated author
     */
    fun updateAuthor(
        id: IdScalar,
        name: String? = null,
    ): AuthorType {
        // Find existing author
        val existingAuthor = authorRepository.findById(id)
            .getOrElse { error -> throw IllegalArgumentException(error.message) }
            ?: throw IllegalArgumentException("Author not found with id: $id")

        // Update the author with provided fields
        val updatedAuthor = existingAuthor.updateEither(
            name = name ?: existingAuthor.name,
        ).getOrElse { error -> throw IllegalArgumentException(error.message) }

        // Save and return
        return authorRepository.save(updatedAuthor)
            .map { AuthorType(it) }
            .getOrElse { error -> throw IllegalStateException(error.message) }
    }

    /**
     * Delete an author by their ID.
     * Note: This will fail if the author has associated books.
     *
     * @param id The ID of the author to delete
     * @return True if the author was deleted, false if they didn't exist
     */
    fun deleteAuthor(id: IdScalar): Boolean {
        // Check if author has books
        if (bookRepository.existsByAuthorId(id)) {
            throw IllegalStateException("Cannot delete author with id $id: author has associated books")
        }

        return authorRepository.deleteById(id)
            .getOrElse { error -> throw IllegalStateException(error.message) }
    }
}
