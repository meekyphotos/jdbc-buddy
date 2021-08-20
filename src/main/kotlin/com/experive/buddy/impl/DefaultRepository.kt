package com.experive.buddy.impl

import com.experive.buddy.Database
import com.experive.buddy.Expression
import com.experive.buddy.QueryResult
import com.experive.buddy.Record
import com.experive.buddy.TableInfo
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.steps.DeleteWhereStep
import com.experive.buddy.steps.InsertSetStep
import com.experive.buddy.steps.SelectFromStep
import com.experive.buddy.steps.UpdateSetStep
import org.springframework.jdbc.core.JdbcTemplate
import reactor.core.publisher.Flux
import kotlin.reflect.KClass

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

    override fun <R : Any> insertInto(entityClass: TableInfo<R>): InsertSetStep<R> {
        return InsertQueryBuilder(entityClass, template, dialect)
    }

    override fun select(vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<Record> {
        return SelectQueryBuilder(template, Record::class, dialect, *selectFieldOrAsterisk)
    }

    override fun <R : Any> selectForEntity(entityClass: KClass<R>, vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<R> {
        return SelectQueryBuilder(template, entityClass, dialect, *selectFieldOrAsterisk)
    }

    override fun <R : Any> update(entityClass: TableInfo<R>): UpdateSetStep<R> {
        return UpdateQueryBuilder(entityClass, template)
    }

    override fun <R : Any> copyIn(entityClass: TableInfo<R>, values: Flux<R>) {
        CopyIn(template, entityClass).execute(values)
    }

    override fun <R : Any> deleteFrom(entityClass: TableInfo<R>): DeleteWhereStep<R> {
        return DeleteQueryBuilder(entityClass, template)
    }
}
