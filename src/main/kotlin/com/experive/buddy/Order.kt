package com.experive.buddy

data class Order(val tableField: TableField<*, *>, val direction: Direction) : QueryPart {
  override fun toSqlFragment(): String = tableField.toSqlFragment() + " " + direction.name + " NULLS LAST"
}