package com.experive.buddy.mapper

import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

internal class ModelMap<K : Any>(private val output: KClass<K>) {
  private val fieldOrder = output.java.declaredFields.filter { !Modifier.isTransient(it.modifiers) }.associateBy { it.name }
  private val allArgsConstructor = output.constructors.firstOrNull { it.parameters.size == fieldOrder.size }
  private val noArgsConstructor = output.constructors.firstOrNull { it.parameters.isEmpty() }
  fun map(input: Any): K {
    return map(AnyMap(input))
  }

  fun map(map: Map<String, Any?>): K {
    if (allArgsConstructor != null) {
      return allArgsConstructor.call(
        *allArgsConstructor.parameters.map { f -> coerceInto(map[f.name], fieldOrder[f.name!!]!!.type) }.toTypedArray()
      )
    } else if (noArgsConstructor != null) {
      val newInstance = noArgsConstructor.call()
      fieldOrder.forEach { (name, javaField) ->
        val value = coerceInto(map[name], javaField.type)
        if (value != null) {
          javaField.isAccessible = true
          javaField.set(newInstance, value)
        }
      }
      return newInstance
    }
    throw IllegalStateException("No constructor for entity: $output")
  }

  private fun coerceInto(any: Any?, f: Class<*>) = if (any != null) {
    val sourceClass = any::class
    val desiredClass = Reflection.createKotlinClass(f)
    SmartMapper.adapters.firstOrNull { it.canAdapt(sourceClass, desiredClass) }?.adapt(any, desiredClass)
  } else {
    any
  }
}