package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import com.experive.buddy.predicates.Predicate

class InPredicate<T>(private val expression: Expression<T>, private val valueExpression: List<Expression<T>>) : Predicate {
  override fun collectValues(): List<Any?> = valueExpression.flatMap { it.collectValues() }

  override fun toSqlFragment(): String {
    return "${expression.toSqlFragment()} in (${valueExpression.joinToString(", ") { it.toSqlFragment() }})"
  }

  override fun toString(): String {
    return "$expression in $valueExpression"
  }


}