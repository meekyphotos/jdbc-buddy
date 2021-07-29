package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import com.experive.buddy.predicates.Predicate

class BetweenPredicate<T>(
  private val expression: Expression<T>,
  private val lower: Expression<T>,
  private val upper: Expression<T>,
) : Predicate {
  override fun collectValues(): List<Any?> = arrayOf(lower, upper).flatMap { it.collectValues() }

  override fun toSqlFragment(): String {
    return "${expression.toSqlFragment()} BETWEEN ${lower.toSqlFragment()} AND ${upper.toSqlFragment()}"
  }

  override fun toString(): String {
    return "$expression between $lower and $upper"
  }


}