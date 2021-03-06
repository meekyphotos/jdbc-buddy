package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import com.experive.buddy.predicates.Predicate

class OperatorOnlyPredicate<T>(private val expression: Expression<T>, private val sign: String) : Predicate {
    override fun collectValues(): List<Any?> = emptyList()

    override fun toQualifiedSqlFragment(): String {
        return "${expression.toQualifiedSqlFragment()} $sign"
    }

    override fun toString(): String {
        return "$expression $sign"
    }
}
