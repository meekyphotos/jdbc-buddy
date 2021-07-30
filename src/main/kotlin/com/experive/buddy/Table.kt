package com.experive.buddy

import com.experive.buddy.impl.TableDetails
import kotlin.reflect.KProperty


class Table<R>(private val details: TableDetails<R>) {
  val enclosingType = details.entityClass
  val alias = details.alias

  fun <I> column(name: String): TableField<R, I>? {
    val column = details.columns[name]
    if (column != null) {
      return TableField(this, column)
    }
    return null
  }

  fun <I> column(prop: KProperty<I>): TableField<R, I> {
    val column = details.columns[prop.name]
    return TableField(this, column!!)
  }

  fun name() = details.name

  fun <I> idColumn(): TableField<R, I>? {
    val column = details.idColumn
    if (column != null) {
      return TableField(this, column)
    }
    return null
  }

  fun asterisk(): Asterisk {
    return Asterisk
  }

}