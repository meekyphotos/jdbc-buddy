package com.experive.buddy

import com.experive.buddy.impl.predicates.BetweenPredicate
import com.experive.buddy.impl.predicates.JsonAccessor
import com.experive.buddy.impl.predicates.OperatorOnlyPredicate
import com.experive.buddy.impl.predicates.OperatorPredicate
import com.experive.buddy.predicates.Predicate
import com.fasterxml.jackson.databind.JsonNode

fun Expression<String?>.like(value: Expression<String?>): Predicate {
    return OperatorPredicate(this, "like", value)
}

fun Expression<String?>.ilike(value: Expression<String?>): Predicate {
    return OperatorPredicate(lower(this), "like", lower(value))
}

fun Expression<Boolean?>.isTrue(): Predicate {
    return OperatorOnlyPredicate(this, "is true")
}

fun Expression<Boolean?>.isFalse(): Predicate {
    return OperatorOnlyPredicate(this, "is false")
}

fun <T : Comparable<T>?> Expression<T>.between(lower: Expression<T>, upper: Expression<T>): Predicate {
    return BetweenPredicate(this, lower, upper)
}

fun <T : Comparable<T>?> Expression<T>.notBetween(lower: Expression<T>, upper: Expression<T>): Predicate {
    return BetweenPredicate(this, lower, upper).not()
}

fun <T : Comparable<T>?> Expression<T>.lessThan(value: Expression<T>): Predicate {
    return OperatorPredicate(this, "<", value)
}

fun <T : Comparable<T>?> Expression<T>.lessOrEqual(value: Expression<T>): Predicate {
    return OperatorPredicate(this, "<=", value)
}

fun <T : Comparable<T>?> Expression<T>.greaterThan(value: Expression<T>): Predicate {
    return OperatorPredicate(this, ">", value)
}

fun <T : Comparable<T>?> Expression<T>.greaterOrEqual(value: Expression<T>): Predicate {
    return OperatorPredicate(this, ">=", value)
}

fun Expression<String?>.like(value: String): Predicate {
    return like(value.asExpression())
}

fun Expression<String?>.ilike(value: String): Predicate {
    return ilike(value.asExpression())
}

fun <T : Comparable<T>?> Expression<T>.between(lower: T, upper: T): Predicate {
    return between(lower.asExpression(), upper.asExpression())
}

fun <T : Comparable<T>?> Expression<T>.notBetween(lower: T, upper: T): Predicate {
    return notBetween(lower.asExpression(), upper.asExpression())
}

fun <T : Comparable<T>?> Expression<T>.lessThan(value: T): Predicate {
    return lessThan(value.asExpression())
}

fun <T : Comparable<T>?> Expression<T>.lessOrEqual(value: T): Predicate {
    return lessOrEqual(value.asExpression())
}

fun <T : Comparable<T>?> Expression<T>.greaterThan(value: T): Predicate {
    return greaterThan(value.asExpression())
}

fun <T : Comparable<T>?> Expression<T>.greaterOrEqual(value: T): Predicate {
    return greaterOrEqual(value.asExpression())
}

fun <T1> Expression<JsonNode>.get(t: String): Expression<T1> {
    return JsonAccessor(this, "->>", t)
}
