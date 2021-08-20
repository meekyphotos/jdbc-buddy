package com.experive.buddy.mapper

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
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
        ByteArrayToJsonNode,
        JsonNodeToString,
        AnyToJsonNode,
        AnyToString,
    )
    private val modelMappers = HashMap<KClass<*>, ModelMap<*>>()

    // todo: add tests coverage for this method! 
    fun <I : Any, O : Any> modelMap(input: I, output: KClass<O>): O {
        val mapper = modelMappers.computeIfAbsent(output) { ModelMap(it) } as ModelMap<O>
        return mapper.map(input)
    }

    fun <I : Any, O : Any> simpleMap(input: I, output: KClass<O>): O? {
        return adapters.firstOrNull { it.canAdapt(input::class, output) }?.adapt(input, output) as O?
    }

    fun <O : Any> modelMap(input: Map<String, Any?>, entityClass: KClass<O>): O {
        val mapper = modelMappers.computeIfAbsent(entityClass) { ModelMap(it) } as ModelMap<O>
        return mapper.map(input)
    }
}
