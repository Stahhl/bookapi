package com.stahhl.bookapi.infrastructure.persistence

import arrow.core.Either
import arrow.core.right
import com.stahhl.bookapi.domain.errors.CoverUploadError
import com.stahhl.bookapi.domain.repositories.CoverUploadRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.CoverUpload
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * JPA implementation of the CoverUploadRepository port.
 */
@Repository
class JpaCoverUploadRepository(
    private val springDataRepository: SpringDataCoverUploadRepository,
) : CoverUploadRepository {
    override fun findById(id: IdScalar): Either<CoverUploadError, CoverUpload?> {
        val entity = springDataRepository.findByIdOrNull(id.value)
            ?: return null.right()
        return entity.toDomain().map { it }
    }

    override fun save(upload: CoverUpload): Either<CoverUploadError, CoverUpload> {
        val entity = CoverUploadEntity.from(upload)
        val saved = springDataRepository.save(entity)
        return saved.toDomain()
    }
}
