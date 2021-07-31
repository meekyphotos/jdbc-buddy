package com.experive.buddy

import com.beust.klaxon.JsonObject
import com.experive.buddy.impl.predicates.BetweenPredicate
import com.experive.buddy.impl.predicates.JsonAccessor
import com.experive.buddy.impl.predicates.OperatorOnlyPredicate
import com.experive.buddy.impl.predicates.OperatorPredicate
import com.experive.buddy.predicates.Predicate

fun Expression<String?>.like(value: Expression<String?>): Predicate = OperatorPredicate(this, "like", value)
fun Expression<String?>.ilike(value: Expression<String?>): Predicate = OperatorPredicate(lower(this), "like", lower(value))
fun Expression<Boolean?>.isTrue(): Predicate = OperatorOnlyPredicate(this, "is true")
fun Expression<Boolean?>.isFalse(): Predicate = OperatorOnlyPredicate(this, "is false")
fun <T : Comparable<T>?> Expression<T>.between(lower: Expression<T>, upper: Expression<T>): Predicate = BetweenPredicate(this, lower, upper)
fun <T : Comparable<T>?> Expression<T>.notBetween(lower: Expression<T>, upper: Expression<T>): Predicate = BetweenPredicate(this, lower, upper).not()
fun <T : Comparable<T>?> Expression<T>.lessThan(value: Expression<T>): Predicate = OperatorPredicate(this, "<", value)
fun <T : Comparable<T>?> Expression<T>.lessOrEqual(value: Expression<T>): Predicate = OperatorPredicate(this, "<=", value)
fun <T : Comparable<T>?> Expression<T>.greaterThan(value: Expression<T>): Predicate = OperatorPredicate(this, ">", value)
fun <T : Comparable<T>?> Expression<T>.greaterOrEqual(value: Expression<T>): Predicate = OperatorPredicate(this, ">=", value)


fun Expression<String?>.like(value: String): Predicate = like(value.asExpression())
fun Expression<String?>.ilike(value: String): Predicate = ilike(value.asExpression())
fun <T : Comparable<T>?> Expression<T>.between(lower: T, upper: T): Predicate = between(lower.asExpression(), upper.asExpression())
fun <T : Comparable<T>?> Expression<T>.notBetween(lower: T, upper: T): Predicate = notBetween(lower.asExpression(), upper.asExpression())
fun <T : Comparable<T>?> Expression<T>.lessThan(value: T): Predicate = lessThan(value.asExpression())
fun <T : Comparable<T>?> Expression<T>.lessOrEqual(value: T): Predicate = lessOrEqual(value.asExpression())
fun <T : Comparable<T>?> Expression<T>.greaterThan(value: T): Predicate = greaterThan(value.asExpression())
fun <T : Comparable<T>?> Expression<T>.greaterOrEqual(value: T): Predicate = greaterOrEqual(value.asExpression())

fun <T1> Expression<JsonObject>.get(t: String): Expression<T1> {
  return JsonAccessor(this, "->>", t)
}
