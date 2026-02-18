package com.stahhl.bookapi.graphql.scalars

import com.stahhl.bookapi.domain.scalars.IsbnScalar
import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.util.Locale

/**
 * GraphQL scalar type for ISBN (International Standard Book Number).
 * Supports both ISBN-10 and ISBN-13 formats with checksum validation.
 */
val graphqlIsbnType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("ISBN")
    .description("A validated ISBN-10 or ISBN-13 identifier")
    .coercing(IsbnCoercing)
    .build()

object IsbnCoercing : Coercing<IsbnScalar, String> {

    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String {
        return when (dataFetcherResult) {
            is IsbnScalar -> dataFetcherResult.value
            is String -> dataFetcherResult
            else -> throw CoercingSerializeException(
                "Expected IsbnScalar but got ${dataFetcherResult::class.simpleName}"
            )
        }
    }

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): IsbnScalar {
        val inputString = when (input) {
            is String -> input
            else -> throw CoercingParseValueException(
                "Expected String but got ${input::class.simpleName}"
            )
        }

        return IsbnScalar.fromEither(inputString).fold(
            ifLeft = { error -> throw CoercingParseValueException(error.message) },
            ifRight = { it }
        )
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): IsbnScalar {
        val stringValue = (input as? StringValue)?.value
            ?: throw CoercingParseLiteralException(
                "Expected StringValue but got ${input::class.simpleName}"
            )

        return IsbnScalar.fromEither(stringValue).fold(
            ifLeft = { error -> throw CoercingParseLiteralException(error.message) },
            ifRight = { it }
        )
    }
}
