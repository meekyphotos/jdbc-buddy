package com.experive.buddy

import com.experive.buddy.impl.Introspector
import com.google.common.primitives.Primitives
import kotlin.reflect.KClass

data class Record(private val data: Map<String, Any>) {
  fun containsKey(key: String): Boolean {
    return data.containsKey(key)
  }

  fun containsValue(value: Any): Boolean {
    return data.containsValue(value)
  }

  operator fun get(key: String): Any? {
    return data[key]
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> into(entityClass: KClass<T>): T? {
    if (entityClass == Record::class) return this as T
    if (Primitives.isWrapperType(entityClass.java) || entityClass.java.isPrimitive) {
      return data.values.firstOrNull() as T?
    }
    return Introspector.analyze(entityClass).newInstance(data) as T
  }
}