package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import com.experive.buddy.predicates.Predicate

class NotInPredicate<T>(private val expression: Expression<T>, private val valueExpression: List<Expression<T>>) : Predicate {
  override fun collectValues(): List<Any?> = valueExpression.flatMap { it.collectValues() }

  override fun toQualifiedSqlFragment(): String {
    return "${expression.toQualifiedSqlFragment()} not in (${valueExpression.joinToString(", ") { it.toQualifiedSqlFragment() }})"
  }

  override fun toString(): String {
    return "$expression not in $valueExpression"
  }

}