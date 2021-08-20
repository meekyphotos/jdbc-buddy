package com.experive.buddy

object Asterisk : Expression<Any?> {
    override fun collectValues(): List<Any?> = emptyList()

    override fun toQualifiedSqlFragment(): String = "*"
}
