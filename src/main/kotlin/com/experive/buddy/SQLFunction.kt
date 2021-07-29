package com.experive.buddy

data class SQLFunction<I, O>(val fnName: String, val value: Expression<I>) : Expression<O> {

  override fun collectValues(): List<Any?> = value.collectValues()

  override fun toSqlFragment(): String = "$fnName(${value.toSqlFragment()})"
  override fun toString(): String {
    return "$fnName($value)"
  }


}