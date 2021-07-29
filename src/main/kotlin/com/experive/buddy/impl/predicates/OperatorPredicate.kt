package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import com.experive.buddy.predicates.Predicate

class OperatorPredicate<T>(private val expression: Expression<T>, private val sign: String, private val valueExpression: Expression<T>) : Predicate {
  override fun collectValues(): List<Any?> = valueExpression.collectValues()

  override fun toSqlFragment(): String {
    return "${expression.toSqlFragment()} $sign ${valueExpression.toSqlFragment()}"
  }

  override fun toString(): String {
    return "$expression $sign $valueExpression"
  }


}