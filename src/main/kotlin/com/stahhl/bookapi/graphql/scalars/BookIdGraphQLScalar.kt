package com.stahhl.bookapi.graphql.scalars

import com.stahhl.bookapi.domain.scalars.IdScalar
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
 * GraphQL scalar type for Book identifiers.
 * Represents a unique book identifier in UUID format.
 */
val graphqlBookIdType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("BookId")
    .description("A unique book identifier (UUID format)")
    .coercing(BookIdCoercing)
    .build()

object BookIdCoercing : Coercing<IdScalar, String> {

    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String {
        return when (dataFetcherResult) {
            is IdScalar -> dataFetcherResult.value.toString()
            is String -> dataFetcherResult
            else -> throw CoercingSerializeException(
                "Expected IdScalar but got ${dataFetcherResult::class.simpleName}"
            )
        }
    }

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): IdScalar {
        val inputString = when (input) {
            is String -> input
            else -> throw CoercingParseValueException(
                "Expected String but got ${input::class.simpleName}"
            )
        }

        return IdScalar.fromEither(inputString).fold(
            ifLeft = { error -> throw CoercingParseValueException(error.message) },
            ifRight = { it }
        )
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): IdScalar {
        val stringValue = (input as? StringValue)?.value
            ?: throw CoercingParseLiteralException(
                "Expected StringValue but got ${input::class.simpleName}"
            )

        return IdScalar.fromEither(stringValue).fold(
            ifLeft = { error -> throw CoercingParseLiteralException(error.message) },
            ifRight = { it }
        )
    }
}
