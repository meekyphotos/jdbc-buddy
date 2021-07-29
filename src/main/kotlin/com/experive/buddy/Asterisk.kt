package com.experive.buddy

class Asterisk : Expression<Any?> {
  override fun collectValues(): List<Any?> = emptyList()

  override fun toSqlFragment(): String = "*"
}