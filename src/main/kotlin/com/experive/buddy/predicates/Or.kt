package com.experive.buddy.predicates

class Or(private vararg val predicates: Predicate) : Predicate {
  override fun collectValues(): List<Any?> = predicates.flatMap { it.collectValues() }

  override fun toQualifiedSqlFragment(): String {
    return "(" + predicates.joinToString(" or ") { it.toQualifiedSqlFragment() } + ")"
  }

}