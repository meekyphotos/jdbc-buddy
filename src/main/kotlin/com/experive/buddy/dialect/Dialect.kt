package com.experive.buddy.dialect

import com.experive.buddy.Expression
import java.sql.ResultSet

interface Dialect {
  fun supportReturning(): Boolean = false
  fun emitPlaceholder(dt: Class<*>): String = "?"
  fun emitOnConflictDoNothing(sb: StringBuilder) {
    sb.append(" ON CONFLICT DO NOTHING")
  }

  fun emitOnDuplicateKeyIgnore(sb: StringBuilder) {
    sb.append(" ON CONFLICT DO NOTHING")
  }

  fun emitReturning(sb: StringBuilder, fields: List<Expression<*>>) = Unit
  fun read(dataTypes: Map<Int, Int>, rs: ResultSet, index: Int): Any? {
    return rs.getObject(index)
  }

  companion object {
    fun of(name: String): Dialect {
      return when (name) {
        "H2" -> H2Dialect()
        "PostgreSQL" -> PostgresDialect()
        else -> DefaultDialect()
      }
    }
  }
}

