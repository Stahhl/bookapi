package com.stahhl.bookapi.infrastructure.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["bookapi.upload.cover.test-ui.enabled=true"])
class CoverUploadTestUiEnabledIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `serves test ui page when feature flag is enabled`() {
        webTestClient.get()
            .uri("/internal/cover-upload-test")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
            .expectBody(String::class.java)
            .value { html ->
                kotlin.test.assertTrue(
                    html.contains("Cover Upload Test UI"),
                    "Expected page title to be present",
                )
                kotlin.test.assertTrue(
                    html.contains("attachBookCover"),
                    "Expected GraphQL mutation reference to be present",
                )
            }
    }
}
