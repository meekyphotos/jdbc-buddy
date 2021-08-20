package com.experive.buddy.predicates

import com.experive.buddy.Expression

interface Predicate : Expression<Boolean> {
    operator fun plus(other: Predicate): Predicate {
        return And(this, other)
    }

    operator fun not(): Predicate = Not(this)

    infix fun or(other: Predicate): Predicate {
        return Or(this, other)
    }

    infix fun and(other: Predicate): Predicate {
        return this + other
    }
}
