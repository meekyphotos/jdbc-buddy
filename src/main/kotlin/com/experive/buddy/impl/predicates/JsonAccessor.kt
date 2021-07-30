package com.experive.buddy.impl.predicates

import com.experive.buddy.Expression
import org.json.JSONObject

class JsonAccessor<T>(private val expression: Expression<JSONObject>, private val operator: String, private val field: String) : Expression<T> {
  override fun collectValues(): List<Any?> = expression.collectValues()

  override fun toQualifiedSqlFragment(): String {
    return "${expression.toQualifiedSqlFragment()}$operator'$field'"
  }

  override fun toString(): String {
    return "$expression$operator'$field'"
  }


}