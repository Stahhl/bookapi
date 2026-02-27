package com.stahhl.bookapi.infrastructure.web

import arrow.core.getOrElse
import com.stahhl.bookapi.domain.repositories.BookRepository
import com.stahhl.bookapi.domain.repositories.CoverUploadRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.CoverUpload
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * REST endpoints for uploading and downloading book cover binaries.
 */
@RestController
@RequestMapping("/api")
class BookCoverController(
    private val coverUploadRepository: CoverUploadRepository,
    private val bookRepository: BookRepository,
    @Value("\${bookapi.upload.cover.directory:\${java.io.tmpdir}/bookapi/covers}")
    private val coverDirectory: String,
    @Value("\${bookapi.upload.cover.max-bytes:5242880}")
    private val maxBytes: Long,
    @Value("\${bookapi.upload.cover.ttl-hours:24}")
    private val ttlHours: Long,
) {
    @PostMapping("/uploads/book-covers", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadBookCover(
        @RequestPart("file") file: FilePart,
    ): Mono<BookCoverUploadResponse> {
        val contentType = file.headers().contentType?.toString()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File content type is required")
        if (!contentType.startsWith("image/")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are supported")
        }

        val uploadId = IdScalar.random()
        val targetPath = resolveTargetPath(uploadId, file.filename())

        return Mono.fromCallable {
            Files.createDirectories(targetPath.parent)
            targetPath
        }.subscribeOn(Schedulers.boundedElastic())
            .flatMap { destination -> file.transferTo(destination).thenReturn(destination) }
            .flatMap { savedPath ->
                Mono.fromCallable {
                    val sizeBytes = Files.size(savedPath)
                    if (sizeBytes <= 0L || sizeBytes > maxBytes) {
                        Files.deleteIfExists(savedPath)
                        throw ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "File size must be between 1 and $maxBytes bytes",
                        )
                    }

                    val now = Instant.now()
                    val upload = CoverUpload.createReadyEither(
                        id = uploadId,
                        storagePath = savedPath.toString(),
                        originalFilename = file.filename(),
                        contentType = contentType,
                        sizeBytes = sizeBytes,
                        uploadedAt = now,
                        expiresAt = now.plus(ttlHours, ChronoUnit.HOURS),
                    ).getOrElse { error ->
                        Files.deleteIfExists(savedPath)
                        throw ResponseStatusException(HttpStatus.BAD_REQUEST, error.message)
                    }

                    val persisted = coverUploadRepository.save(upload)
                        .getOrElse { error ->
                            Files.deleteIfExists(savedPath)
                            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error.message)
                        }

                    BookCoverUploadResponse(
                        uploadId = persisted.id.toString(),
                        originalFilename = persisted.originalFilename,
                        contentType = persisted.contentType,
                        sizeBytes = persisted.sizeBytes,
                        expiresAt = persisted.expiresAt.toString(),
                    )
                }.subscribeOn(Schedulers.boundedElastic())
            }
    }

    @GetMapping("/books/{bookId}/cover")
    fun downloadBookCover(
        @PathVariable bookId: UUID,
    ): Mono<ResponseEntity<FileSystemResource>> =
        Mono.fromCallable {
            val book = bookRepository.findById(IdScalar.fromUUID(bookId))
                .getOrElse { error -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error.message) }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with id: $bookId")

            val cover = book.cover
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book has no cover")

            val path = Paths.get(cover.storagePath)
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cover file not found")
            }

            val mediaType = try {
                MediaType.parseMediaType(cover.contentType)
            } catch (_: IllegalArgumentException) {
                MediaType.APPLICATION_OCTET_STREAM
            }

            ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${book.id}-cover\"")
                .body(FileSystemResource(path))
        }.subscribeOn(Schedulers.boundedElastic())

    private fun resolveTargetPath(uploadId: IdScalar, originalFilename: String): Path {
        val basePath = Paths.get(coverDirectory).toAbsolutePath().normalize()
        val extension = extractSafeExtension(originalFilename)
        val fileName = "${uploadId.value}$extension"
        val targetPath = basePath.resolve(fileName).normalize()
        if (!targetPath.startsWith(basePath)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload filename")
        }
        return targetPath
    }

    private fun extractSafeExtension(filename: String): String {
        val rawExtension = filename.substringAfterLast('.', "")
        if (rawExtension.isBlank()) {
            return ""
        }

        val sanitized = rawExtension.lowercase()
        return if (sanitized.matches(Regex("[a-z0-9]{1,10}"))) {
            ".$sanitized"
        } else {
            ""
        }
    }
}

data class BookCoverUploadResponse(
    val uploadId: String,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val expiresAt: String,
)
