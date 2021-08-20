package com.experive.buddy

class SQLFunction<I, O>(private val fnName: String, private vararg val value: Expression<out I>) : Expression<O> {

    override fun collectValues(): List<Any?> = value.flatMap { it.collectValues() }

    override fun toQualifiedSqlFragment(): String {
        return "$fnName(${value.joinToString(", ") { it.toQualifiedSqlFragment() }})"
    }

    override fun toString(): String {
        return "$fnName($value)"
    }
}
