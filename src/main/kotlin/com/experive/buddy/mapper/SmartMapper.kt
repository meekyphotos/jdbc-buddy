package com.experive.buddy.mapper

import kotlin.reflect.KClass

object SmartMapper {
  internal val adapters = listOf(
    IdentityAdapter(),
    NumberToLong,
    NumberToInt,
    NumberToDouble,
    NumberToFloat,
    NumberToShort,
    NumberToByte,
    NumberToChar,
    StringToLong,
    StringToInt,
    StringToDouble,
    StringToFloat,
    StringToShort,
    StringToByte,
    DateToLocalTime,
    DateToLocalDateTime,
    DateToLocalDate,
    StringToJsonNode,
    AnyToJsonNode,
    AnyToString,
  )
  private val modelMappers = HashMap<KClass<*>, ModelMap<*>>()
  fun <I : Any, O : Any> modelMap(input: I, output: KClass<O>): O {
    val mapper = modelMappers.computeIfAbsent(output) { ModelMap(it) } as ModelMap<O>
    return mapper.map(input)
  }

  fun <O : Any> modelMap(input: Map<String, Any?>, entityClass: KClass<O>): O {
    val mapper = modelMappers.computeIfAbsent(entityClass) { ModelMap(it) } as ModelMap<O>
    return mapper.map(input)
  }
}

