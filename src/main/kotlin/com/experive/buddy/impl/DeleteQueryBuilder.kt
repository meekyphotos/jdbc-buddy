package com.experive.buddy.impl

import com.experive.buddy.Table
import com.experive.buddy.predicates.Predicate
import com.experive.buddy.steps.Delete
import com.experive.buddy.steps.DeleteWhereStep
import org.springframework.jdbc.core.JdbcTemplate

class DeleteQueryBuilder<R>(
  private val table: Table<R>,
  private val template: JdbcTemplate
) : DeleteWhereStep<R> {
  private val predicates = ArrayList<Predicate>()
  override fun where(vararg predicates: Predicate): Delete<R> {
    this.predicates.addAll(predicates)
    return this
  }

  private fun collectParameters(): ArrayList<Any?> {
    val variables = ArrayList<Any?>()
    predicates.forEach { variables.addAll(it.collectValues()) }
    return variables
  }

  override fun execute(): Int {
    return template.update(toSQL(), *collectParameters().toTypedArray())
  }

  override fun toSQL(): String {
    val sb = StringBuilder()
    sb.append("DELETE FROM ${table.name()} ${table.alias}")
    if (predicates.isNotEmpty()) {
      sb.append(" WHERE ")
      predicates.joinTo(sb, " AND ") { it.toQualifiedSqlFragment() }
    }
    return sb.toString()
  }
}