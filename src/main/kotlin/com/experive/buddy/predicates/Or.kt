package com.experive.buddy.predicates

class Or(private vararg val predicates: Predicate) : Predicate {
  override fun collectValues(): List<Any?> = predicates.flatMap { it.collectValues() }

  override fun toSqlFragment(): String {
    return "(" + predicates.joinToString(" or ") { it.toSqlFragment() } + ")"
  }

}