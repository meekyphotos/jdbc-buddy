package com.experive.buddy.predicates

class Not(private val predicate: Predicate) : Predicate {
  override fun collectValues(): List<Any?> = predicate.collectValues()

  override fun toSqlFragment(): String {
    return "not (" + predicate.toSqlFragment() + ")"
  }

  override fun toString(): String {
    return "Not($predicate)"
  }


}