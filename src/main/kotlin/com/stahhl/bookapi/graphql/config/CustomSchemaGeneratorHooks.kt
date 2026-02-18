package com.stahhl.bookapi.graphql.config

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.stahhl.bookapi.domain.scalars.IdScalar
import com.stahhl.bookapi.domain.scalars.IsbnScalar
import com.stahhl.bookapi.graphql.scalars.graphqlBookIdType
import com.stahhl.bookapi.graphql.scalars.graphqlIsbnType
import graphql.schema.GraphQLType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Custom schema generator hooks to register domain scalars as GraphQL scalar types.
 * This ensures that IsbnScalar and IdScalar are represented as custom scalars
 * in the generated GraphQL schema, preserving type information for clients.
 */
@Component
class CustomSchemaGeneratorHooks : SchemaGeneratorHooks {

    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        return when (type.classifier as? KClass<*>) {
            IsbnScalar::class -> graphqlIsbnType
            IdScalar::class -> graphqlBookIdType
            else -> null
        }
    }
}
