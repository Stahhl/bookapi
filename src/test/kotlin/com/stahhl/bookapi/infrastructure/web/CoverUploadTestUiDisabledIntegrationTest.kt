package com.stahhl.bookapi.infrastructure.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoverUploadTestUiDisabledIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `returns 404 when test ui is disabled`() {
        webTestClient.get()
            .uri("/internal/cover-upload-test")
            .exchange()
            .expectStatus().isNotFound
    }
}
