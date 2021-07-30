package com.experive.buddy.predicates

class Not(private val predicate: Predicate) : Predicate {
  override fun collectValues(): List<Any?> = predicate.collectValues()

  override fun toQualifiedSqlFragment(): String {
    return "not (" + predicate.toQualifiedSqlFragment() + ")"
  }

  override fun toString(): String {
    return "Not($predicate)"
  }


}