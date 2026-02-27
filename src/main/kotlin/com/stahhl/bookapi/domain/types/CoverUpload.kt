package com.stahhl.bookapi.domain.types

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.stahhl.bookapi.domain.errors.CoverUploadError
import com.stahhl.bookapi.domain.scalars.IdScalar
import java.time.Instant

/**
 * Staged book cover upload metadata.
 * Binary payload is stored externally; this type tracks lifecycle and validation.
 */
data class CoverUpload private constructor(
    val id: IdScalar,
    val storagePath: String,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val uploadedAt: Instant,
    val expiresAt: Instant,
    val consumedAt: Instant?,
) {
    fun isConsumed(): Boolean = consumedAt != null

    fun isExpired(now: Instant = Instant.now()): Boolean = now.isAfter(expiresAt)

    /**
     * Rehydrates persisted consumption metadata from storage.
     */
    fun withConsumedAt(consumedAt: Instant?): CoverUpload = copy(consumedAt = consumedAt)

    context(Raise<CoverUploadError>)
    fun consume(now: Instant = Instant.now()): CoverUpload {
        ensure(!isExpired(now)) { CoverUploadError.Expired(id.toString()) }
        ensure(!isConsumed()) { CoverUploadError.AlreadyConsumed(id.toString()) }
        return copy(consumedAt = now)
    }

    fun consumeEither(now: Instant = Instant.now()): Either<CoverUploadError, CoverUpload> =
        either { consume(now) }

    companion object {
        context(Raise<CoverUploadError>)
        fun createReady(
            id: IdScalar,
            storagePath: String,
            originalFilename: String,
            contentType: String,
            sizeBytes: Long,
            uploadedAt: Instant,
            expiresAt: Instant,
        ): CoverUpload {
            val errors = mutableListOf<String>()

            if (storagePath.isBlank()) {
                errors.add("Storage path cannot be blank")
            }
            if (originalFilename.isBlank()) {
                errors.add("Original filename cannot be blank")
            }
            if (contentType.isBlank()) {
                errors.add("Content type cannot be blank")
            } else if (!contentType.startsWith("image/")) {
                errors.add("Content type must be an image/* type")
            }
            if (sizeBytes <= 0) {
                errors.add("Upload size must be greater than 0 bytes")
            }
            if (!expiresAt.isAfter(uploadedAt)) {
                errors.add("Expiration must be after upload time")
            }

            ensure(errors.isEmpty()) {
                CoverUploadError.ValidationFailed(errors)
            }

            return CoverUpload(
                id = id,
                storagePath = storagePath.trim(),
                originalFilename = originalFilename.trim(),
                contentType = contentType.trim().lowercase(),
                sizeBytes = sizeBytes,
                uploadedAt = uploadedAt,
                expiresAt = expiresAt,
                consumedAt = null,
            )
        }

        fun createReadyEither(
            id: IdScalar,
            storagePath: String,
            originalFilename: String,
            contentType: String,
            sizeBytes: Long,
            uploadedAt: Instant,
            expiresAt: Instant,
        ): Either<CoverUploadError, CoverUpload> = either {
            createReady(
                id = id,
                storagePath = storagePath,
                originalFilename = originalFilename,
                contentType = contentType,
                sizeBytes = sizeBytes,
                uploadedAt = uploadedAt,
                expiresAt = expiresAt,
            )
        }
    }
}
