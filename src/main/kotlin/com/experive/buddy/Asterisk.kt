package com.experive.buddy

class Asterisk : Expression<Any?> {
  override fun collectValues(): List<Any?> = emptyList()

  override fun toQualifiedSqlFragment(): String = "*"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Asterisk) return false
    return true
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }


}