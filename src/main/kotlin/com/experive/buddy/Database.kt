package com.experive.buddy

import com.experive.buddy.impl.DefaultRepository
import com.experive.buddy.impl.Introspector
import com.experive.buddy.steps.*
import org.springframework.jdbc.core.JdbcTemplate

@Suppress("UNCHECKED_CAST")
interface Database {
  fun <E, I> byId(id: I, entityClass: TableInfo<E>): Select<E> {
    return selectFrom(entityClass).where(entityClass.idColumn<I>()!!.eq(id))
  }

  // raw sql execution
  fun execute(sql: String, vararg args: Any?)
  fun fetch(sql: String, vararg args: Any?): QueryResult<Record>

  fun <R> insertInto(entityClass: TableInfo<R>): InsertSetStep<R>
  fun <R> selectFrom(entityClass: TableInfo<R>): SelectWhereStep<R> = selectForEntity(entityClass.enclosingType, entityClass.asterisk()).from(entityClass)
  fun <R> selectCount(entityClass: TableInfo<R>): SelectWhereStep<R> = selectForEntity(entityClass.enclosingType, count()).from(entityClass)
  fun select(vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<Record>
  fun <R> selectForEntity(entityClass: Class<R>, vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<R>
  fun <R> update(entityClass: TableInfo<R>): UpdateSetStep<R>
  fun <R> delete(entityClass: TableInfo<R>): DeleteWhereStep<R> = deleteFrom(entityClass)
  fun <R> deleteFrom(entityClass: TableInfo<R>): DeleteWhereStep<R>

  fun <E> delete(entity: E): Delete<E> {
    val tableInfo = TableInfo(Introspector.analyze(entity!!::class.java as Class<E>))
    val idColumn: TableField<E, Any> = tableInfo.idColumn()!!
    val idValue = idColumn.valueOf(entity)!!
    return deleteFrom(tableInfo).where(idColumn.eq(idValue))
  }

  fun <R, I> deleteById(id: I, entityClass: TableInfo<R>): Delete<R> {
    return deleteFrom(entityClass).where(entityClass.idColumn<I>()!!.eq(id))
  }

  fun <E> update(entity: E): Update<E> {
    val details = Introspector.analyze(entity!!::class.java as Class<E>)
    val tableInfo = TableInfo(details)
    val idColumn: TableField<E, Any> = tableInfo.idColumn()!!
    val updatableColumns = details.updatableColumns
    val update = update(tableInfo)
    for (updatableColumn in updatableColumns) {
      update.set(updatableColumn.asFieldOf(tableInfo), updatableColumn.getValue(entity))
    }
    update as UpdateSetMoreStep
    val idValue = idColumn.valueOf(entity)!!
    return update.where(idColumn.eq(idValue))
  }

  fun <E> persist(entity: E): InsertMoreStep<E> {
    val details = Introspector.analyze(entity!!::class.java as Class<E>)
    val tableInfo = TableInfo(details)
    val insertableColumns = details.insertableColumns
    val insert = insertInto(tableInfo)
    for (updatableColumn in insertableColumns) {
      insert.set(updatableColumn.asFieldOf(tableInfo), updatableColumn.getValue(entity))
    }
    return insert as InsertMoreStep<E>
  }

  fun <E> persistMany(entities: Collection<E>): InsertValuesStep<E> {
    val details = Introspector.analyze(entities.first()!!::class.java as Class<E>)
    val tableInfo = TableInfo(details)
    val insertableColumns = details.insertableColumns
    val insert = insertInto(tableInfo).columns(*insertableColumns.map { it.asFieldOf(tableInfo) }.toTypedArray())
    for (entity in entities) {
      insert.values(*insertableColumns.map { it.getValue(entity) }.toTypedArray())
    }
    return insert
  }

  companion object {
    fun using(template: JdbcTemplate) = DefaultRepository(template)
  }
}

