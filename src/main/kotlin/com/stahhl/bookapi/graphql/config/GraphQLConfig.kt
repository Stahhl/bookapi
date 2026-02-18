package com.stahhl.bookapi.graphql.config

import com.expediagroup.graphql.generator.scalars.IDValueUnboxer
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * GraphQL configuration for handling inline value classes.
 * 
 * Since IdScalar and IsbnScalar are Kotlin inline value classes,
 * graphql-java needs a ValueUnboxer to extract the underlying values
 * during serialization.
 */
@Configuration
class GraphQLConfig {

    /**
     * Custom ValueUnboxer that handles our domain scalar value classes.
     * Extends IDValueUnboxer to maintain support for GraphQL's built-in ID type.
     */
    @Bean
    fun idValueUnboxer(): IDValueUnboxer = CustomValueUnboxer()
}

class CustomValueUnboxer : IDValueUnboxer() {

    override fun unbox(value: Any?): Any? {
        return when (value) {
            is IdScalar -> value.value.toString()
            is IsbnScalar -> value.value
            else -> super.unbox(value)
        }
    }
}
