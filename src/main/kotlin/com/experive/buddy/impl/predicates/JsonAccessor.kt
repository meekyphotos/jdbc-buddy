package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import com.fasterxml.jackson.databind.JsonNode

class JsonAccessor<T>(private val expression: Expression<JsonNode>, private val operator: String, private val field: String) : Expression<T> {
    override fun collectValues(): List<Any?> = expression.collectValues()

    override fun toQualifiedSqlFragment(): String {
        return "${expression.toQualifiedSqlFragment()}$operator'$field'"
    }

    override fun toString(): String {
        return "$expression$operator'$field'"
    }
}
