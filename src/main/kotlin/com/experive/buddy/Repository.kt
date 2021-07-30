package com.experive.buddy

import com.experive.buddy.impl.Introspector
import com.experive.buddy.steps.*

@Suppress("UNCHECKED_CAST")
interface Repository {
  fun <E, I> byId(id: I, entityClass: Table<E>): Select<E> {
    return selectFrom(entityClass).where(entityClass.idColumn<I>()!!.eq(id))
  }

  // raw sql execution
  fun execute(sql: String, vararg args: Any?)
  fun fetch(sql: String, vararg args: Any?): QueryResult<Record>

  fun <R> insertInto(entityClass: Table<R>): InsertSetStep<R>
  fun <R> selectFrom(entityClass: Table<R>): SelectWhereStep<R> = selectForEntity(entityClass.enclosingType, entityClass.asterisk()).from(entityClass)
  fun <R> selectCount(entityClass: Table<R>): SelectWhereStep<R> = selectForEntity(entityClass.enclosingType, count()).from(entityClass)
  fun select(vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<Record>
  fun <R> selectForEntity(entityClass: Class<R>, vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<R>
  fun <R> update(entityClass: Table<R>): UpdateSetStep<R>
  fun <R> delete(entityClass: Table<R>): DeleteWhereStep<R> = deleteFrom(entityClass)
  fun <R> deleteFrom(entityClass: Table<R>): DeleteWhereStep<R>

  fun <E> delete(entity: E): Delete<E> {
    val table = Table(Introspector.analyze(entity!!::class.java as Class<E>))
    val idColumn: TableField<E, Any> = table.idColumn()!!
    val idValue = idColumn.valueOf(entity)!!
    return deleteFrom(table).where(idColumn.eq(idValue))
  }

  fun <R, I> deleteById(id: I, entityClass: Table<R>): Delete<R> {
    return deleteFrom(entityClass).where(entityClass.idColumn<I>()!!.eq(id))
  }

  fun <E> update(entity: E): Update<E> {
    val details = Introspector.analyze(entity!!::class.java as Class<E>)
    val table = Table(details)
    val idColumn: TableField<E, Any> = table.idColumn()!!
    val updatableColumns = details.updatableColumns
    val update = update(table)
    for (updatableColumn in updatableColumns) {
      update.set(updatableColumn.asFieldOf(table), updatableColumn.getValue(entity))
    }
    update as UpdateSetMoreStep
    val idValue = idColumn.valueOf(entity)!!
    return update.where(idColumn.eq(idValue))
  }

  fun <E> persist(entity: E): InsertMoreStep<E> {
    val details = Introspector.analyze(entity!!::class.java as Class<E>)
    val table = Table(details)
    val insertableColumns = details.insertableColumns
    val insert = insertInto(table)
    for (updatableColumn in insertableColumns) {
      insert.set(updatableColumn.asFieldOf(table), updatableColumn.getValue(entity))
    }
    return insert as InsertMoreStep<E>
  }

  fun <E> persistMany(entities: Collection<E>): InsertValuesStep<E> {
    val details = Introspector.analyze(entities.first()!!::class.java as Class<E>)
    val table = Table(details)
    val insertableColumns = details.insertableColumns
    val insert = insertInto(table).columns(*insertableColumns.map { it.asFieldOf(table) }.toTypedArray())
    for (entity in entities) {
      insert.values(*insertableColumns.map { it.getValue(entity) }.toTypedArray())
    }
    return insert
  }
}

