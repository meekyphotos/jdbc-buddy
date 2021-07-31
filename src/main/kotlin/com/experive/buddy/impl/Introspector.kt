package com.experive.buddy.impl

import com.experive.buddy.TableField

import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

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

  fun <T> asFieldOf(t: com.experive.buddy.TableInfo<T>): TableField<T, Any?> {
    return TableField(t, this)
  }
}

data class TableDetails<T>(
  val name: String,

  val columns: Map<String, ColumnDetails>,
  val entityClass: Class<T>
) {
  val insertableColumns: List<ColumnDetails> = columns.values.filter { it.insertable }
  val updatableColumns: List<ColumnDetails> = columns.values.filter { it.updatable }
  val idColumn: ColumnDetails? = columns.values.firstOrNull { it.id }
  val fieldOrder: List<String> = entityClass.declaredFields.filter { !Modifier.isTransient(it.modifiers) }.map { it.name }
  val emptyConstructor: Constructor<*>? = entityClass.constructors.firstOrNull { Modifier.isPublic(it.modifiers) && it.parameterCount == 0 }
  val allArgsConstructor: Constructor<*>? = entityClass.constructors.firstOrNull { Modifier.isPublic(it.modifiers) && it.parameterCount == fieldOrder.size }
  val alias: String = name + Introspector.classCounter.getAndIncrement()
  fun newInstance(map: Map<String, Any>): Any {
    if (allArgsConstructor != null) {
      return allArgsConstructor.newInstance(
        *fieldOrder.map { map[columns[it]!!.name] }.toTypedArray()
      )
    } else if (emptyConstructor != null) {
      val newInstance = emptyConstructor.newInstance()
      fieldOrder.forEach {
        val dbValue = map[columns[it]!!.name]
        if (dbValue != null) {
          val declaredField = entityClass.getDeclaredField(it)
          declaredField.isAccessible = true
          declaredField.set(newInstance, dbValue)
        }
      }
      return newInstance
    }
    throw IllegalStateException("No constructor for entity: $entityClass")
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
  private val cache = HashMap<Class<*>, TableDetails<*>>()
  fun <T> analyze(java: Class<T>): TableDetails<T> {
    return cache.computeIfAbsent(java) {
      val name: String = normalizeTableName(java)
      val fields = java.declaredFields
        .filter { !Modifier.isTransient(it.modifiers) }
      val getter = java.declaredMethods
        .filter { !Modifier.isStatic(it.modifiers) && (it.name.startsWith("get") || it.name.startsWith("is")) }
        .filter { !Modifier.isTransient(it.modifiers) }
        .associateBy { cleanMethodName(it.name) }
      val staticGetterKt = java.declaredMethods
        .filter { Modifier.isStatic(it.modifiers) && (it.name.startsWith("get") || it.name.startsWith("is")) }
        .filter { !Modifier.isTransient(it.modifiers) }
        .associateBy { cleanMethodName(it.name) }
      val columns = HashMap<String, ColumnDetails>()
      fields.forEach {
        val fieldName = it.name
        it.isAccessible = true
        val possibleElements = arrayOf(it, getter[it.name], staticGetterKt[it.name]).filterNotNull()
        var found = false
        for (element in possibleElements) {
          val meta = read(element)
          val (isIdentifier, isGenerated, column) = meta
          if (meta.wasAnnotated()) {
            columns[fieldName] = build(column, it, fieldName, isIdentifier, isGenerated)
            found = true
            break
          }
        }
        if (!found) {
          columns[fieldName] = build(null, it, fieldName, isIdentifier = false, isGenerated = false)
        }
      }
      TableDetails(name, columns, java)
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