package com.experive.buddy

import com.experive.buddy.impl.ColumnDetails

open class TableField<R : Any, T>(private val tableInfo: TableInfo<R>, private val property: ColumnDetails) : Expression<T> {
  val name: String = property.name
  val dataType = property.type

  override fun toQualifiedSqlFragment(): String = tableInfo.alias + "." + name
  override fun toSqlFragment(): String = name

  override fun toString(): String {
    return name
  }

  fun valueOf(instance: R): T? {
    return property.field.get(instance) as T?
  }

}