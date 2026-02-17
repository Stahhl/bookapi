package com.stahhl.bookapi.domain.types

import com.stahhl.bookapi.domain.scalars.IdScalar

data class Book(
    val id: IdScalar,
    val title: String,
    val author: String,
)
