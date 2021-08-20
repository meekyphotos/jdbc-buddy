package com.experive.buddy.expressions

import com.experive.buddy.Expression

class CastExpression<I, O>(private val e: Expression<I>, private val dataType: String) : Expression<O> {
    override fun toQualifiedSqlFragment(): String = "CAST (${e.toQualifiedSqlFragment()} as $dataType)"
    override fun collectValues(): List<Any?> = e.collectValues()
}
