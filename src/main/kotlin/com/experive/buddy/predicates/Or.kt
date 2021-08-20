package com.experive.buddy.predicates

class Or(private vararg val predicates: Predicate) : Predicate {
    init {
        check(predicates.isNotEmpty()) { "Or requires at least one condition" }
    }

    override fun collectValues(): List<Any?> = predicates.flatMap { it.collectValues() }

    override fun toQualifiedSqlFragment(): String {
        return if (predicates.size > 1)
            "(" + predicates.joinToString(" or ") { it.toQualifiedSqlFragment() } + ")"
        else
            predicates.joinToString(" or ") { it.toQualifiedSqlFragment() }
    }
}
