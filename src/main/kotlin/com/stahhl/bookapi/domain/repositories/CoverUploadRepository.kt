package com.stahhl.bookapi.domain.repositories

import arrow.core.Either
import com.stahhl.bookapi.domain.errors.CoverUploadError
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.CoverUpload

/**
 * Repository interface for staged cover upload metadata persistence.
 */
interface CoverUploadRepository {
    /**
     * Finds a staged upload by ID.
     */
    fun findById(id: IdScalar): Either<CoverUploadError, CoverUpload?>

    /**
     * Saves a staged upload (insert or update).
     */
    fun save(upload: CoverUpload): Either<CoverUploadError, CoverUpload>
}
