package com.experive.buddy.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

interface Adapter {
  fun adapt(v: Any, desiredClass: KClass<*>): Any
  fun canAdapt(source: KClass<*>, desiredClass: KClass<*>): Boolean
}

internal class IdentityAdapter : Adapter {
  override fun adapt(v: Any, desiredClass: KClass<*>): Any = v

  override fun canAdapt(source: KClass<*>, desiredClass: KClass<*>): Boolean = desiredClass.isSuperclassOf(source)
}

inline fun <reified A : Any, reified B : Any> adapter(crossinline adaptFn: (A) -> B): Adapter {
  return object : Adapter {
    override fun adapt(v: Any, desiredClass: KClass<*>): Any = adaptFn(v as A)

    override fun canAdapt(source: KClass<*>, desiredClass: KClass<*>): Boolean = source.isSubclassOf(A::class) && desiredClass.isSuperclassOf(B::class)

  }
}

internal val NumberToLong = adapter<Number, Long> { it.toLong() }
internal val NumberToInt = adapter<Number, Int> { it.toInt() }
internal val NumberToDouble = adapter<Number, Double> { it.toDouble() }
internal val NumberToFloat = adapter<Number, Float> { it.toFloat() }
internal val NumberToShort = adapter<Number, Short> { it.toShort() }
internal val NumberToByte = adapter<Number, Byte> { it.toByte() }
internal val NumberToChar = adapter<Number, Char> { it.toChar() }

internal val StringToLong = adapter<String, Long> { it.toLong() }
internal val StringToInt = adapter<String, Int> { it.toInt() }
internal val StringToDouble = adapter<String, Double> { it.toDouble() }
internal val StringToFloat = adapter<String, Float> { it.toFloat() }
internal val StringToShort = adapter<String, Short> { it.toShort() }
internal val StringToByte = adapter<String, Byte> { it.toByte() }

internal val DateToLocalTime = adapter<Date, LocalTime> { Time(it.time).toLocalTime() }
internal val DateToLocalDateTime = adapter<Date, LocalDateTime> { Timestamp(it.time).toLocalDateTime() }
internal val DateToLocalDate = adapter<Date, LocalDate> { java.sql.Date(it.time).toLocalDate() }

internal val AnyToString = adapter<Any, String> { it.toString() }
internal val objectMapper = ObjectMapper()
internal val StringToJsonNode = adapter<String, JsonNode> { objectMapper.readTree(it) }
internal val ByteArrayToJsonNode = adapter<ByteArray, JsonNode> { objectMapper.readTree(it) }
internal val JsonNodeToString = adapter<JsonNode, String> { objectMapper.writeValueAsString(it) }
internal val AnyToJsonNode = adapter<Any, JsonNode> { objectMapper.valueToTree(it) }

fun json(init: ObjectNode.() -> Unit): JsonNode {
  val node = objectMapper.createObjectNode()
  init(node)
  return node
}

fun jsonArray(init: ArrayNode.() -> Unit): JsonNode {
  val node = objectMapper.createArrayNode()
  init(node)
  return node
}
