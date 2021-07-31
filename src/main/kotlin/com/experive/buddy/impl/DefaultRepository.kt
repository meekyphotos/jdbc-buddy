package com.experive.buddy.impl

import com.experive.buddy.*
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.steps.DeleteWhereStep
import com.experive.buddy.steps.InsertSetStep
import com.experive.buddy.steps.SelectFromStep
import com.experive.buddy.steps.UpdateSetStep
import org.springframework.jdbc.core.JdbcTemplate

class DefaultRepository(private val template: JdbcTemplate) : Database {
  private val dbName: String = template.dataSource!!.connection.use {
    it.metaData.databaseProductName
  }
  private val dialect = Dialect.of(dbName)

  override fun execute(sql: String, vararg args: Any?) {
    template.update(sql, *args)
  }

  override fun fetch(sql: String, vararg args: Any?): QueryResult<Record> {
    return StreamQueryResult(template.queryForStream(sql, RecordMapper(dialect), *args))
  }

  override fun <R> insertInto(entityClass: TableInfo<R>): InsertSetStep<R> {
    return InsertQueryBuilder(entityClass, template, dialect)
  }

  override fun select(vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<Record> {
    return SelectQueryBuilder(template, Record::class.java, dialect, *selectFieldOrAsterisk)
  }

  override fun <R> selectForEntity(entityClass: Class<R>, vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<R> {
    return SelectQueryBuilder(template, entityClass, dialect, *selectFieldOrAsterisk)
  }

  override fun <R> update(entityClass: TableInfo<R>): UpdateSetStep<R> {
    return UpdateQueryBuilder(entityClass, template)
  }

  override fun <R> deleteFrom(entityClass: TableInfo<R>): DeleteWhereStep<R> {
    return DeleteQueryBuilder(entityClass, template)
  }
}