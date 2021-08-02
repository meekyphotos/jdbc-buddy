package com.experive.buddy.mapper

import java.lang.reflect.Field
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

internal class AnyMap(private val source: Any) : AbstractMap<String, Any?>() {
  private val map = HashMap<String, Field>()

  init {
    source::class.declaredMemberProperties.forEach {
      val field = it.javaField!!
      field.isAccessible = true
      map[field.name] = field
    }
  }

  override fun get(key: String): Any? {
    return map[key]?.get(source)
  }

  override val entries: Set<Map.Entry<String, Any?>>
    get() = map.entries.map { AnyMapEntry(source, it.value) }.toSet()


  class AnyMapEntry(private val source: Any, private val field: Field) : Map.Entry<String, Any?> {
    override val key: String = field.name

    private fun value(): Any? {
      field.isAccessible = true
      return field.get(source)
    }

    override val value: Any?
      get() = this.value()
  }
}