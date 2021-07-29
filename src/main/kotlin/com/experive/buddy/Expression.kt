package com.experive.buddy

import com.experive.buddy.expressions.AsExpression
import com.experive.buddy.impl.predicates.InPredicate
import com.experive.buddy.impl.predicates.NotInPredicate
import com.experive.buddy.impl.predicates.OperatorOnlyPredicate
import com.experive.buddy.impl.predicates.OperatorPredicate
import com.experive.buddy.predicates.Predicate

interface Expression<T> : QueryPart {
  fun collectValues(): List<Any?> = emptyList()

  fun `as`(name: String): Expression<T> {
    return AsExpression(this, name)
  }

  fun eq(value: Expression<T>): Predicate = OperatorPredicate(this, "=", value)
  fun notEqual(value: Expression<T>): Predicate = OperatorPredicate(this, "<>", value)
  fun `in`(vararg values: Expression<T>): Predicate = InPredicate(this, values.toList())
  fun notIn(vararg values: Expression<T>): Predicate = NotInPredicate(this, values.toList())

  fun isNull(): Predicate = OperatorOnlyPredicate(this, "is null")
  fun isNotNull(): Predicate = OperatorOnlyPredicate(this, "is not null")
  fun `in`(vararg values: T): Predicate = `in`(*values.map { it.asExpression() }.toTypedArray())

  fun eq(value: T): Predicate {
    if (value is Expression<*>)
      return OperatorPredicate(this, "=", value as Expression<T>)
    return eq(value.asExpression())
  }

  fun notEqual(value: T): Predicate = notEqual(value.asExpression())
  fun notIn(vararg values: T): Predicate = notIn(*values.map { it.asExpression() }.toTypedArray())
}
