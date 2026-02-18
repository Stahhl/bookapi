package com.stahhl.bookapi.graphql.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Represents a GraphQL response for testing purposes.
 * Provides convenient accessors for data and errors.
 */
data class GraphQLResponse(
    val rawJson: JsonNode,
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * The data portion of the GraphQL response, or null if not present.
     */
    val data: JsonNode?
        get() = rawJson.get("data")

    /**
     * The errors array from the GraphQL response, or empty list if no errors.
     */
    val errors: List<GraphQLError>
        get() = rawJson.get("errors")?.map { errorNode ->
            GraphQLError(
                message = errorNode.get("message")?.asText() ?: "",
                path = errorNode.get("path")?.map { it.asText() } ?: emptyList(),
                extensions = errorNode.get("extensions")
            )
        } ?: emptyList()

    /**
     * Check if the response has any errors.
     */
    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    /**
     * Extract a value from the data at the given path.
     * Path uses dot notation, e.g., "book.author.name"
     */
    fun <T> extractData(path: String, type: Class<T>): T? {
        var current: JsonNode? = data
        for (segment in path.split(".")) {
            current = current?.get(segment)
            if (current == null || current.isNull) return null
        }
        return objectMapper.treeToValue(current, type)
    }

    /**
     * Extract a value from the data at the given path as a specific type.
     */
    inline fun <reified T> extractData(path: String): T? = extractData(path, T::class.java)

    /**
     * Get a JsonNode at the given path within the data.
     */
    fun getDataNode(path: String): JsonNode? {
        var current: JsonNode? = data
        for (segment in path.split(".")) {
            current = current?.get(segment)
            if (current == null || current.isNull) return null
        }
        return current
    }

    /**
     * Check if a data field is null.
     */
    fun isNull(path: String): Boolean {
        val node = getDataNode(path)
        return node == null || node.isNull
    }

    companion object {
        private val objectMapper = jacksonObjectMapper()

        fun fromJson(json: String): GraphQLResponse {
            return GraphQLResponse(objectMapper.readTree(json))
        }
    }
}

/**
 * Represents a single GraphQL error.
 */
data class GraphQLError(
    val message: String,
    val path: List<String>,
    val extensions: JsonNode?,
) {
    /**
     * Get the error classification if present in extensions.
     */
    val classification: String?
        get() = extensions?.get("classification")?.asText()
}
