package com.stahhl.bookapi.infrastructure.web

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Minimal internal test UI for manually validating cover upload and attach flow.
 * This controller is disabled by default and must be explicitly enabled via feature flag.
 */
@RestController
@ConditionalOnProperty(
    prefix = "bookapi.upload.cover.test-ui",
    name = ["enabled"],
    havingValue = "true",
)
class CoverUploadTestUiController {
    @GetMapping("/internal/cover-upload-test")
    fun page(): ResponseEntity<Resource> =
        ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(ClassPathResource("internal/cover-upload-test.html"))
}
