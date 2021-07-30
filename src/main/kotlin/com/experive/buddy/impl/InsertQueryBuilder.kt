package com.experive.buddy.impl

import com.experive.buddy.*
import com.experive.buddy.impl.results.SafeQueryResult
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.steps.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.support.GeneratedKeyHolder

internal class InsertQueryBuilder<R>(
  private val entityClass: Table<R>,
  private val template: JdbcTemplate,
  private val dbName: String
) : InsertSetStep<R>, InsertValuesStep<R>, InsertMoreStep<R>, InsertReturningStep<R>, InsertResultStep<R> {

  private var onConflictDoNothing: Boolean = false
  private var onDuplicateKeyIgnore: Boolean = false
  private var returning: Boolean = false
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
        values.joinTo(sb, ", ") { "?" }
        sb.append(")")
      } else {
        sb.append("VALUES ")
        records.joinTo(sb, ", ") { "(" + it.joinToString(", ") { "?" } + ")" }
      }
    }
    if (onConflictDoNothing || onDuplicateKeyIgnore) {
      sb.append(" ON CONFLICT DO NOTHING")
    }
    if (supportsReturning() && returningFields.isNotEmpty()) {
      sb.append(" RETURNING ")
      returningFields.joinTo(sb, ", ") { it.toSqlFragment() }
    }
    return sb.toString()
  }

  override fun returning(): InsertResultStep<R> {
    returningFields.add(Asterisk())
    returning = true
    return this
  }

  private fun supportsReturning(): Boolean {
    return dbName != "H2"
  }

  override fun returning(vararg selectFieldOrAsterisk: Expression<*>): InsertResultStep<R> {
    returningFields.addAll(selectFieldOrAsterisk)
    returning = true
    return this
  }

  override fun <T> set(tableField: TableField<R, T>, value: T): InsertMoreStep<R> {
    check(select == null) { "Cannot use set method when using select insert" }
    check(records.isEmpty()) { "Cannot use set method when using multiple records" }
    check(columns.size == values.size) { "Cannot use set after calling columns" }
    check(!columns.contains(tableField)) { "Cannot set same field multiple times" }
    columns.add(tableField)
    values.add(value)
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
    records.add(values)
    return this
  }

  override fun select(selectQuery: Select<R>): InsertOnDuplicateStep<R> {
    check(values.isEmpty()) { "Cannot use select after you've used values method" }
    check(records.isEmpty()) { "Cannot use select after you've used set method" }
    this.select = selectQuery
    return this
  }

  override fun fetchInto(): QueryResult<R> = fetch().map { it.into(entityClass.enclosingType) }

  override fun fetchOneInto(): R? = fetchOne()?.into(entityClass.enclosingType)

  override fun fetchSingleInto(): R = fetchSingle().into(entityClass.enclosingType)

  override fun fetch(): QueryResult<Record> {
    check(returning) { "Fetch is only allowed when using returning" }
    return if (supportsReturning()) {
      StreamQueryResult(template.queryForStream(toSQL(), RecordMapper(), *collectParameters().toTypedArray()))
    } else {
      val keys = GeneratedKeyHolder()
      template.update(PreparedStatementCreator {
        val returningColumns = ArrayList<String>()
        if (returningFields.contains(Asterisk())) {
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