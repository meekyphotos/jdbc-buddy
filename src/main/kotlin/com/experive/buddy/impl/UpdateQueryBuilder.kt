package com.experive.buddy.impl

import com.experive.buddy.TableField
import com.experive.buddy.TableInfo
import com.experive.buddy.predicates.Predicate
import com.experive.buddy.steps.Update
import com.experive.buddy.steps.UpdateSetMoreStep
import com.experive.buddy.steps.UpdateSetStep
import org.springframework.jdbc.core.JdbcTemplate

internal class UpdateQueryBuilder<R : Any>(
    private val tableInfo: TableInfo<R>,
    private val template: JdbcTemplate
) : UpdateSetStep<R>, UpdateSetMoreStep<R>, Update<R> {
    private val columns = ArrayList<TableField<R, *>>()
    private val values = ArrayList<Any?>()
    private val predicates = ArrayList<Predicate>()

    private fun collectParameters(): ArrayList<Any?> {
        val variables = ArrayList<Any?>()
        variables.addAll(values)
        predicates.forEach { variables.addAll(it.collectValues()) }
        return variables
    }

    override fun execute(): Int {
        return template.update(toSQL(), *collectParameters().toTypedArray())
    }

    override fun toSQL(): String {
        check(columns.isNotEmpty()) { "You need to specify at least one column to update" }
        val sb = StringBuilder()
        sb.append("UPDATE ${tableInfo.name()} ${tableInfo.alias} SET ")
        columns.joinTo(sb, ", ") { "${it.name}=?" }
        if (predicates.isNotEmpty()) {
            sb.append(" WHERE ")
            predicates.joinTo(sb, " AND ") { it.toQualifiedSqlFragment() }
        }
        return sb.toString()
    }

    override fun where(vararg predicates: Predicate): Update<R> {
        this.predicates.addAll(predicates)
        return this
    }

    override fun <Q> set(tableField: TableField<R, Q>, value: Q): UpdateSetMoreStep<R> {
        columns.add(tableField)
        values.add(value)
        return this
    }
}
