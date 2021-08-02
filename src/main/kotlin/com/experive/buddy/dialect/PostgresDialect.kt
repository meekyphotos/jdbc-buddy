package com.experive.buddy.dialect

import com.experive.buddy.Expression
import com.experive.buddy.mapper.SmartMapper
import com.fasterxml.jackson.databind.JsonNode
import org.postgresql.util.PGobject
import java.sql.ResultSet
import java.sql.Types

internal class PostgresDialect : Dialect {
  override fun supportReturning(): Boolean = true

  override fun emitPlaceholder(dt: Class<*>): String {
    return if (dt == JsonNode::class.java) {
      "?::jsonb"
    } else {
      "?"
    }
  }

  override fun emitOnConflictDoNothing(sb: StringBuilder) {
    sb.append(" ON CONFLICT DO NOTHING")
  }

  override fun emitOnDuplicateKeyIgnore(sb: StringBuilder) {
    sb.append(" ON CONFLICT DO NOTHING")
  }

  override fun emitReturning(sb: StringBuilder, fields: List<Expression<*>>) {
    sb.append(" RETURNING ")
    fields.joinTo(sb, ", ") { it.toSqlFragment() }
  }

  override fun read(dataTypes: Map<Int, Int>, rs: ResultSet, index: Int): Any? {
    return if (dataTypes[index] == Types.OTHER) {
      val obj = rs.getObject(index, PGobject::class.java)
      if (obj != null && obj.value != null) {
        return SmartMapper.simpleMap(obj.value!!, JsonNode::class)
      } else {
        null
      }
    } else {
      super.read(dataTypes, rs, index)
    }


  }
}