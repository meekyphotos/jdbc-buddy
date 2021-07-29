package com.experive.buddy.predicates

class And(private vararg val predicates: Predicate) : Predicate {

  override fun collectValues(): List<Any?> = predicates.flatMap { it.collectValues() }

  override fun toSqlFragment(): String {
    return predicates.joinToString(" and ") { it.toSqlFragment() }
  }
}