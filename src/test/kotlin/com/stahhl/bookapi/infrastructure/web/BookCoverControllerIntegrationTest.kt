package com.stahhl.bookapi.infrastructure.web

import com.stahhl.bookapi.infrastructure.persistence.SpringDataCoverUploadRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = [
        "bookapi.upload.cover.directory=/tmp/bookapi-test-covers",
        "bookapi.upload.cover.max-bytes=2048",
        "bookapi.upload.cover.ttl-hours=1",
    ],
)
class BookCoverControllerIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var springDataCoverUploadRepository: SpringDataCoverUploadRepository

    private val uploadDirectory: Path = Path.of("/tmp/bookapi-test-covers")

    @BeforeEach
    fun setUp() {
        springDataCoverUploadRepository.deleteAll()
        Files.createDirectories(uploadDirectory)
        uploadDirectory.toFile().listFiles()?.forEach { it.delete() }
    }

    @AfterEach
    fun tearDown() {
        uploadDirectory.toFile().listFiles()?.forEach { it.delete() }
    }

    @Test
    fun `uploads image and persists staged metadata`() {
        val multipartData = MultipartBodyBuilder().apply {
            part("file", object : ByteArrayResource("fake-image-bytes".toByteArray()) {
                override fun getFilename(): String = "cover.png"
            }).contentType(MediaType.IMAGE_PNG)
        }.build()

        webTestClient.post()
            .uri("/api/uploads/book-covers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartData))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.uploadId").isNotEmpty
            .jsonPath("$.contentType").isEqualTo("image/png")
            .jsonPath("$.originalFilename").isEqualTo("cover.png")

        assertEquals(1, springDataCoverUploadRepository.count())
    }

    @Test
    fun `rejects non-image uploads`() {
        val multipartData = MultipartBodyBuilder().apply {
            part("file", object : ByteArrayResource("plain-text".toByteArray()) {
                override fun getFilename(): String = "cover.txt"
            }).contentType(MediaType.TEXT_PLAIN)
        }.build()

        webTestClient.post()
            .uri("/api/uploads/book-covers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartData))
            .exchange()
            .expectStatus().isBadRequest
    }
}
