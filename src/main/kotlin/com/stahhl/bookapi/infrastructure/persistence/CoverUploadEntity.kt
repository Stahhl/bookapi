package com.stahhl.bookapi.infrastructure.persistence

import arrow.core.Either
import arrow.core.raise.either
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.CoverUpload
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for staged cover upload metadata.
 */
@Entity
@Table(name = "cover_uploads")
class CoverUploadEntity(
    @Id
    val id: UUID,

    @Column(name = "storage_path", nullable = false, length = 1024)
    val storagePath: String,

    @Column(name = "original_filename", nullable = false, length = 255)
    val originalFilename: String,

    @Column(name = "content_type", nullable = false, length = 100)
    val contentType: String,

    @Column(name = "size_bytes", nullable = false)
    val sizeBytes: Long,

    @Column(name = "uploaded_at", nullable = false)
    val uploadedAt: Instant,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "consumed_at", nullable = true)
    val consumedAt: Instant? = null,
) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        storagePath = "",
        originalFilename = "",
        contentType = "",
        sizeBytes = 0,
        uploadedAt = Instant.EPOCH,
        expiresAt = Instant.EPOCH,
        consumedAt = null,
    )

    fun toDomain(): Either<com.stahhl.bookapi.domain.errors.CoverUploadError, CoverUpload> = either {
        CoverUpload.createReadyEither(
            id = IdScalar.fromUUID(id),
            storagePath = storagePath,
            originalFilename = originalFilename,
            contentType = contentType,
            sizeBytes = sizeBytes,
            uploadedAt = uploadedAt,
            expiresAt = expiresAt,
        ).bind().withConsumedAt(consumedAt)
    }

    companion object {
        fun from(upload: CoverUpload): CoverUploadEntity = CoverUploadEntity(
            id = upload.id.value,
            storagePath = upload.storagePath,
            originalFilename = upload.originalFilename,
            contentType = upload.contentType,
            sizeBytes = upload.sizeBytes,
            uploadedAt = upload.uploadedAt,
            expiresAt = upload.expiresAt,
            consumedAt = upload.consumedAt,
        )
    }
}
