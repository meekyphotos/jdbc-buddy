package com.experive.buddy.expressions

import com.experive.buddy.Expression

open class LiteralExpression<I>(private val value: I?) : Expression<I> {

    override fun collectValues(): List<Any?> = listOf(value)

    override fun toQualifiedSqlFragment(): String = "?"
    override fun toString(): String {
        return "$value"
    }
}
