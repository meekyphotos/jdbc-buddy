package com.experive.buddy.dialect

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.sql.ResultSet
import java.sql.Types

internal class H2Dialect : Dialect {

  override fun emitPlaceholder(dt: Class<*>): String {
    return if (dt == JsonObject::class.java || dt == JsonArray::class.java) {
      "? FORMAT JSON"
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

  override fun read(dataTypes: Map<Int, Int>, rs: ResultSet, index: Int): Any? {
    if (dataTypes[index] == Types.OTHER) {
      val jsonAsByteArray = rs.getObject(index)
      return if (jsonAsByteArray != null && jsonAsByteArray is ByteArray) {
        val str = StringBuilder(String(jsonAsByteArray))
        val parse = Parser.default().parse(str)
        if (str.startsWith("{")) {
          parse as JsonObject
        } else
          parse as JsonArray<*>
      } else {
        null
      }
    }
    return super.read(dataTypes, rs, index)
  }
}