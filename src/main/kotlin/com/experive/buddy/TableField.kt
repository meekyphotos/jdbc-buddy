package com.experive.buddy

import com.experive.buddy.impl.ColumnDetails

data class TableField<R, T>(private val table: Table<R>, private val property: ColumnDetails) : Expression<T> {
  val name: String
    get() = property.name

  override fun toSqlFragment(): String = table.alias + "." + name

  override fun toString(): String {
    return name
  }

  fun valueOf(instance: R): T? {
    return property.field.get(instance) as T?
  }

}