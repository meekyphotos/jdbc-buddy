package com.experive.buddy.impl

import com.experive.buddy.*
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.steps.DeleteWhereStep
import com.experive.buddy.steps.InsertSetStep
import com.experive.buddy.steps.SelectFromStep
import com.experive.buddy.steps.UpdateSetStep
import org.springframework.jdbc.core.JdbcTemplate

class DefaultRepository(private val template: JdbcTemplate) : Repository {
  private val dbName: String = template.dataSource!!.connection.use {
    it.metaData.databaseProductName
  }

  override fun execute(sql: String, vararg args: Any?) {
    template.update(sql, *args)
  }

  override fun fetch(sql: String, vararg args: Any?): QueryResult<Record> {
    return StreamQueryResult(template.queryForStream(sql, RecordMapper(), *args))
  }

  override fun <R> insertInto(entityClass: Table<R>): InsertSetStep<R> {
    return InsertQueryBuilder(entityClass, template, dbName)
  }

  override fun <R> select(vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<R> {
    return SelectQueryBuilder(template, dbName, *selectFieldOrAsterisk)
  }

  override fun <R> update(entityClass: Table<R>): UpdateSetStep<R> {
    return UpdateQueryBuilder(entityClass, template, dbName)
  }

  override fun <R> deleteFrom(entityClass: Table<R>): DeleteWhereStep<R> {
    return DeleteQueryBuilder(entityClass, template, dbName)
  }
}