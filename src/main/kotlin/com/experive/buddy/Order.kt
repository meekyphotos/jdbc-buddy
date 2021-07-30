package com.experive.buddy

data class Order(val tableField: TableField<*, *>, val direction: Direction) : QueryPart {
  override fun toQualifiedSqlFragment(): String = tableField.toQualifiedSqlFragment() + " " + direction.name + " NULLS LAST"
}