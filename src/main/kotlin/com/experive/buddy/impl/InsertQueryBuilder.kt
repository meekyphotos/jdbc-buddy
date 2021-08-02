package com.experive.buddy.impl

import com.experive.buddy.*
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.impl.results.SafeQueryResult
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.mapper.SmartMapper
import com.experive.buddy.steps.*
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder

internal class InsertQueryBuilder<R : Any>(
  private val entityClass: TableInfo<R>,
  private val template: JdbcTemplate,
  private val dialect: Dialect
) : InsertSetStep<R>, InsertValuesStep<R>, InsertMoreStep<R>, InsertReturningStep<R>, InsertResultStep<R> {

  private var onConflictDoNothing: Boolean = false
  private var onDuplicateKeyIgnore: Boolean = false
  private var returningFields = ArrayList<Expression<*>>()
  private var columns = ArrayList<TableField<R, *>>()
  private var values = ArrayList<Any?>()
  private var records = ArrayList<Array<out Any?>>()
  private var select: Select<R>? = null

  override fun onConflictDoNothing(): InsertReturningStep<R> {
    this.onConflictDoNothing = true
    return this
  }

  override fun onDuplicateKeyIgnore(): InsertReturningStep<R> {
    this.onDuplicateKeyIgnore = true
    return this
  }

  override fun execute(): Int {
    return template.update(toSQL(), *collectParameters().toTypedArray())
  }

  private fun collectParameters(): ArrayList<Any?> {
    val variables = ArrayList<Any?>()
    variables.addAll(values)
    records.forEach { variables.addAll(it) }
    if (select != null)
      variables.addAll(select!!.collectParameters())
    return variables
  }

  override fun toSQL(): String {
    check(columns.isNotEmpty()) { "You need to defined at least one column to insert" }
    check(values.isNotEmpty() || records.isNotEmpty() || select != null) { "Missing values to insert" }
    val sb = StringBuilder()
    sb.append("INSERT INTO ${entityClass.name()} (")
    columns.joinTo(sb, ", ") { it.name }
    sb.append(") ")
    if (select != null) {
      sb.append(select!!.toSQL())
    } else {
      if (records.isEmpty()) {
        sb.append("VALUES (")
        sb.append(printRowPlaceholder())
        sb.append(")")
      } else {
        sb.append("VALUES ")
        records.joinTo(sb, ", ") { "(" + printRowPlaceholder() + ")" }
      }
    }
    if (onConflictDoNothing) {
      dialect.emitOnConflictDoNothing(sb)
    } else if (onDuplicateKeyIgnore) {
      dialect.emitOnDuplicateKeyIgnore(sb)
    }

    if (returningFields.isNotEmpty()) {
      dialect.emitReturning(sb, returningFields)
    }
    return sb.toString()
  }

  private fun printRowPlaceholder() = columns.joinToString(", ") { formatColumnPlaceholder(it) }

  private fun formatColumnPlaceholder(it: TableField<R, *>): String {
    return dialect.emitPlaceholder(it.dataType)
  }

  override fun returning(): InsertResultStep<R> {
    returningFields.add(Asterisk)
    return this
  }


  override fun returning(vararg selectFieldOrAsterisk: Expression<*>): InsertResultStep<R> {
    returningFields.addAll(selectFieldOrAsterisk)
    return this
  }

  override fun <T> set(tableField: TableField<R, T>, value: T): InsertMoreStep<R> {
    check(select == null) { "Cannot use set method when using select insert" }
    check(records.isEmpty()) { "Cannot use set method when using multiple records" }
    check(columns.size == values.size) { "Cannot use set after calling columns" }
    check(!columns.contains(tableField)) { "Cannot set same field multiple times" }
    columns.add(tableField)

    when (value) {
      is JsonNode -> {
        values.add(SmartMapper.simpleMap(value, String::class))
      }
      else -> {
        values.add(value)
      }
    }

    return this
  }

  override fun columns(vararg tableFields: TableField<R, *>): InsertValuesStep<R> {
    check(values.isEmpty()) { "Columns are specified automatically when you use set(Field, value)" }
    columns.addAll(tableFields)
    return this
  }

  override fun values(vararg values: Any?): InsertValuesStep<R> {
    check(select == null) { "Cannot specify values when using select insert" }
    check(this.values.isEmpty()) { "Cannot mix and match set(Field, value) with values" }
    check(this.columns.size == values.size) { "Specified values don't match the number of columns" }

    records.add(this.columns.mapIndexed { index, _ ->
      when (val value = values[index]) {
        is JsonNode -> {
          SmartMapper.simpleMap(value, String::class)
        }
        else -> {
          value
        }
      }
    }.toTypedArray())
    return this
  }

  override fun select(selectQuery: Select<R>): InsertOnDuplicateStep<R> {
    check(values.isEmpty()) { "Cannot use select after you've used values method" }
    check(records.isEmpty()) { "Cannot use select after you've used set method" }
    this.select = selectQuery
    return this
  }

  override fun fetchInto(): QueryResult<R> = fetch().map { it.into(entityClass.enclosingType)!! }

  override fun fetchOneInto(): R? = fetchOne()?.into(entityClass.enclosingType)

  override fun fetchSingleInto(): R = fetchSingle().into(entityClass.enclosingType)!!

  override fun fetch(): QueryResult<Record> {
    check(returningFields.isNotEmpty()) { "Fetch is only allowed when using returning" }
    return if (dialect.supportReturning()) {
      StreamQueryResult(template.queryForStream(toSQL(), RecordMapper(dialect), *collectParameters().toTypedArray()))
    } else {
      val keys = GeneratedKeyHolder()
      template.update({
        val returningColumns = ArrayList<String>()
        if (returningFields.contains(Asterisk)) {
          returningColumns.add(entityClass.idColumn<Any?>()!!.toSqlFragment())
        } else {
          returningColumns.addAll(returningFields.map { f -> f.toSqlFragment() })
        }
        val psmt = it.prepareStatement(toSQL(), returningColumns.toTypedArray())
        collectParameters().forEachIndexed { index, any -> psmt.setObject(index + 1, any) }
        psmt
      }, keys)
      SafeQueryResult(keys.keyList.map { Record(it) })
    }

  }


}