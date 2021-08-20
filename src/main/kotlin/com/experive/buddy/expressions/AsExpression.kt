package com.experive.buddy.expressions

import com.experive.buddy.Expression

class AsExpression<I>(private val e: Expression<I>, private val alias: String) : Expression<I> {
    override fun toQualifiedSqlFragment(): String = e.toQualifiedSqlFragment() + " AS " + alias
    override fun collectValues(): List<Any?> = e.collectValues()
}
