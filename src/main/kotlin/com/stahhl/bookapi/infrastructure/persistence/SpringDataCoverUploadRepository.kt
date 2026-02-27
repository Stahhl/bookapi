package com.stahhl.bookapi.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA repository for CoverUploadEntity.
 */
interface SpringDataCoverUploadRepository : JpaRepository<CoverUploadEntity, UUID>
