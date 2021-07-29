package com.experive.buddy

import com.experive.buddy.impl.Introspector
import com.google.common.primitives.Primitives

class Record(private val data: Map<String, Any>) {
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
  fun <T> into(entityClass: Class<T>): T {
    if (Primitives.isWrapperType(entityClass) || entityClass.isPrimitive) {
      return data.values.firstOrNull() as T
    }
    return Introspector.analyze(entityClass).newInstance(data) as T
  }
}