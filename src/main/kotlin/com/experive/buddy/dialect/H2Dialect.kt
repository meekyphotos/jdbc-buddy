package com.experive.buddy.dialect

import com.experive.buddy.mapper.SmartMapper
import com.fasterxml.jackson.databind.JsonNode
import java.sql.ResultSet
import java.sql.Types

internal class H2Dialect : Dialect {

    override fun emitPlaceholder(dt: Class<*>): String {
        return if (dt == JsonNode::class.java) {
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
                return SmartMapper.simpleMap(jsonAsByteArray, JsonNode::class)
            } else {
                null
            }
        }
        return super.read(dataTypes, rs, index)
    }
}
