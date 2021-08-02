package com.experive.buddy.impl

import com.experive.buddy.TableField
import com.experive.buddy.TableInfo
import com.experive.buddy.mapper.SmartMapper
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import kotlin.reflect.KClass

data class ColumnDetails(
  val name: String,
  val type: Class<*>,
  val field: Field,
  val updatable: Boolean,
  val insertable: Boolean,
  val generated: Boolean,
  val id: Boolean
) {
  fun <E> getValue(entity: E): Any? = field.get(entity)

  fun <T : Any> asFieldOf(t: TableInfo<T>): TableField<T, Any?> {
    return TableField(t, this)
  }
}

data class TableDetails<T : Any>(
  val name: String,

  val columns: Map<String, ColumnDetails>,
  val entityClass: KClass<T>
) {
  val insertableColumns: List<ColumnDetails> = columns.values.filter { it.insertable }
  val updatableColumns: List<ColumnDetails> = columns.values.filter { it.updatable }
  val idColumn: ColumnDetails? = columns.values.firstOrNull { it.id }
  private val fieldOrder: List<Field> = entityClass.java.declaredFields.filter { !Modifier.isTransient(it.modifiers) }
  val alias: String = name + Introspector.classCounter.getAndIncrement()
  fun newInstance(map: Map<String, Any>): Any {
    return SmartMapper.modelMap(fieldOrder.associate { it.name to map[columns[it.name]!!.name] }, entityClass)
  }
}

internal data class Metadata(val identifier: Boolean, val generated: Boolean, val column: Column?) {
  fun wasAnnotated(): Boolean = identifier || generated || column != null
}

internal fun read(el: AnnotatedElement): Metadata {
  return Metadata(
    el.isAnnotationPresent(Id::class.java),
    el.isAnnotationPresent(GeneratedValue::class.java),
    el.getAnnotation(Column::class.java)
  )
}

internal object Introspector {
  val classCounter = AtomicLong()
  private val cache = HashMap<KClass<*>, TableDetails<*>>()
  fun <T : Any> analyze(kclass: KClass<T>): TableDetails<T> {
    return cache.computeIfAbsent(kclass) {
      val java = it.java
      val name: String = normalizeTableName(java)
      val fields = java.declaredFields
        .filter { f -> !Modifier.isTransient(f.modifiers) }
      val getter = java.declaredMethods
        .filter { f -> !Modifier.isStatic(f.modifiers) && (f.name.startsWith("get") || f.name.startsWith("is")) }
        .filter { f -> !Modifier.isTransient(f.modifiers) }
        .associateBy { cleanMethodName(java.name) }
      val staticGetterKt = java.declaredMethods
        .filter { f -> Modifier.isStatic(f.modifiers) && (f.name.startsWith("get") || f.name.startsWith("is")) }
        .filter { f -> !Modifier.isTransient(f.modifiers) }
        .associateBy { cleanMethodName(java.name) }
      val columns = HashMap<String, ColumnDetails>()
      fields.forEach { field ->
        val fieldName = field.name
        field.isAccessible = true
        val possibleElements = arrayOf(field, getter[field.name], staticGetterKt[field.name]).filterNotNull()
        var found = false
        for (element in possibleElements) {
          val meta = read(element)
          val (isIdentifier, isGenerated, column) = meta
          if (meta.wasAnnotated()) {
            columns[fieldName] = build(column, field, fieldName, isIdentifier, isGenerated)
            found = true
            break
          }
        }
        if (!found) {
          columns[fieldName] = build(null, field, fieldName, isIdentifier = false, isGenerated = false)
        }
      }
      TableDetails(name, columns, it)
    } as TableDetails<T>
  }

  private fun <T> normalizeTableName(java: Class<T>) = if (java.isAnnotationPresent(Table::class.java)) {
    val table = java.getAnnotation(Table::class.java)
    table.name.ifBlank {
      normalizeName(java.simpleName.substringAfterLast('.'))
    }
  } else {
    normalizeName(java.simpleName.substringAfterLast('.'))
  }

  private fun build(column: Column?, field: Field, fieldName: String, isIdentifier: Boolean, isGenerated: Boolean): ColumnDetails {
    return if (column != null) {
      ColumnDetails(
        column.name,
        field.type,
        field,
        !(isIdentifier || isGenerated) && column.updatable,
        !(isGenerated) && column.insertable,
        isGenerated,
        isIdentifier
      )
    } else {
      ColumnDetails(normalizeName(fieldName), field.type, field, !(isIdentifier || isGenerated), !isGenerated, isGenerated, isIdentifier)
    }
  }

  private fun cleanMethodName(name: String): String {
    val out = StringBuilder(name.length)
    var isGetter = false
    var firstActual = true
    for ((index, c) in name.withIndex()) {
      when {
        index == 0 && c == 'g' -> {
          isGetter = true
        }
        index == 0 && c == 'i' -> {
          isGetter = false
        }
        index == 1 || index == 2 && isGetter -> continue
        firstActual -> {
          firstActual = false
          out.append(c.lowercaseChar())
        }
        c == '$' -> break
        else -> {
          out.append(c)
        }
      }
    }
    return out.toString()
  }

  private fun normalizeName(name: String): String {
    val out = StringBuilder(name.length)
    for ((index, c) in name.withIndex()) {
      when {
        index == 0 -> {
          out.append(c.lowercaseChar())
        }
        c.isUpperCase() -> {
          out.append('_').append(c.lowercaseChar())
        }
        else -> {
          out.append(c)
        }
      }
    }
    return out.toString()
  }

}