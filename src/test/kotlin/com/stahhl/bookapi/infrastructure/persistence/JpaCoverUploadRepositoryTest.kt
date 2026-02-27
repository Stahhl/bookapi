package com.stahhl.bookapi.infrastructure.persistence

import com.stahhl.bookapi.domain.repositories.CoverUploadRepository
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.types.CoverUpload
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@Import(JpaCoverUploadRepository::class)
class JpaCoverUploadRepositoryTest {

    @Autowired
    private lateinit var repository: CoverUploadRepository

    @Autowired
    private lateinit var springDataRepository: SpringDataCoverUploadRepository

    @BeforeEach
    fun setUp() {
        springDataRepository.deleteAll()
    }

    @Test
    fun `saves and finds upload`() {
        val upload = CoverUpload.createReadyEither(
            id = IdScalar.random(),
            storagePath = "/tmp/bookapi-test-covers/${IdScalar.random()}.png",
            originalFilename = "cover.png",
            contentType = "image/png",
            sizeBytes = 100,
            uploadedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(3600),
        ).getOrNull()!!

        val saved = repository.save(upload)
        assertTrue(saved.isRight())

        val found = repository.findById(upload.id)
        assertTrue(found.isRight())
        assertNotNull(found.getOrNull())
        assertEquals(upload.id, found.getOrNull()!!.id)
        assertEquals(upload.storagePath, found.getOrNull()!!.storagePath)
    }

    @Test
    fun `returns null when upload is not found`() {
        val result = repository.findById(IdScalar.random())

        assertTrue(result.isRight())
        assertNull(result.getOrNull())
    }
}
