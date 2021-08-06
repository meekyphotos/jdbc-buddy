package com.experive.buddy

import com.experive.buddy.impl.DefaultRepository
import com.experive.buddy.impl.Introspector
import com.experive.buddy.steps.*
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
interface Database {
  /**
   * This method is a shortcut to quickly create a
   * query that select from the given table using the id
   *
   * At least one field needs to be marked with id annotation
   *
   * @param id type should match the type of the id column
   * @param entityClass table structure used to determine the `id` and the `from` table
   * @see select
   * @see javax.persistence.Id
   * @since 1.0.0
   */
  fun <E : Any, I> byId(id: I, entityClass: TableInfo<E>): Select<E> {
    return selectFrom(entityClass).where(entityClass.idColumn<I>()!!.eq(id))
  }

  /**
   * Execute the given update sql
   *
   * @param sql - sql string that needs to be execute
   * @param args - if sql string has `?` placeholder, specify the list of arguments
   * @since 1.0.0
   */
  fun execute(sql: String, vararg args: Any?)

  /**
   * Execute the given sql as query
   *
   * @param sql sql string that needs to be execute
   * @param args if sql string has `?` placeholder, specify the list of arguments
   * @see QueryResult
   * @since 1.0.0
   */
  fun fetch(sql: String, vararg args: Any?): QueryResult<Record>

  fun <R : Any> insertInto(entityClass: TableInfo<R>): InsertSetStep<R>

  /**
   * Starts the creation of a select query
   *
   * ```sql
   * SELECT *
   * FROM entityClass
   * ```
   *
   * @param entityClass specifies the class of the entities retrieve using [FetchableQuery.fetchInto]
   * @sample byId
   * @since 1.0.0
   */
  fun <R : Any> selectFrom(entityClass: TableInfo<R>): SelectWhereStep<R> = selectForEntity(entityClass.enclosingType, entityClass.asterisk()).from(entityClass)

  /**
   * Starts the creation of a select count query
   *
   * ```sql
   * SELECT count(*)
   * FROM entityClass
   * ```
   *
   * @param entityClass specifies the class of the entities retrieve using QueryResult#fetchInto
   */
  fun <R : Any> selectCount(entityClass: TableInfo<R>): SelectWhereStep<R> = selectForEntity(entityClass.enclosingType, count()).from(entityClass)

  /**
   * Starts the creation of a select query
   *
   * Allows to specify which fields are selected, since from is not specified at this time, the type of the query defaults to [Record]
   */
  fun select(vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<Record>

  /**
   * Starts the creation of a select query
   *
   * Unlike [select] this method, request the class of the entity which will be used to map entities.
   * The specified class has no effect on the generated query, it's only used to get the correct type
   *
   * @sample selectFrom
   * @sample selectCount
   */
  fun <R : Any> selectForEntity(entityClass: KClass<R>, vararg selectFieldOrAsterisk: Expression<*>): SelectFromStep<R>
  fun <R : Any> update(entityClass: TableInfo<R>): UpdateSetStep<R>
  fun <R : Any> delete(entityClass: TableInfo<R>): DeleteWhereStep<R> = deleteFrom(entityClass)

  /**
   * Copy data between a file and a table
   *
   * COPY moves data between PostgreSQL tables and standard file-system files appending the data to whatever is in the table already.
   * By default it will specify all the insertable columns of the entity
   *
   * Check official documentation for caveat: [https://www.postgresql.org/docs/current/sql-copy.html]
   * @since 1.4.0
   */
  fun <R : Any> copyIn(entityClass: TableInfo<R>, values: Iterable<R>)

  /**
   * Starts the creation of a deletion query
   *
   * @sample deleteById
   * @see delete
   */
  fun <R : Any> deleteFrom(entityClass: TableInfo<R>): DeleteWhereStep<R>

  /**
   * Delete an entity by its id
   *
   * To avoid a reflective access to determine the value of the id, consider using deleteById
   *
   * @see deleteById
   * @since 1.0.0
   */
  fun <E : Any> delete(entity: E): Delete<E> {
    val tableInfo = TableInfo(Introspector.analyze(entity::class as KClass<E>))
    val idColumn: TableField<E, Any> = tableInfo.idColumn()!!
    val idValue = idColumn.valueOf(entity)!!
    return deleteFrom(tableInfo).where(idColumn.eq(idValue))
  }

  /**
   * Delete a row by id
   *
   * @see delete
   * @since 1.0.0
   */
  fun <R : Any, I> deleteById(id: I, entityClass: TableInfo<R>): Delete<R> {
    return deleteFrom(entityClass).where(entityClass.idColumn<I>()!!.eq(id))
  }

  /**
   * Updates a *single* entity.
   *
   * The generated query takes in consideration only the updatable columns.
   * It uses the id as condition for the update, so the output will be something like:
   * ```sql
   * UPDATE table SET c1 = ?, c2 = ?, c3 = ? WHERE id = ?
   * ```
   * @since 1.0.0
   */
  fun <E : Any> update(entity: E): Update<E> {
    val details = Introspector.analyze(entity::class as KClass<E>)
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

  /**
   * Persist a single entity.
   *
   * The generated query takes in consideration only the insertable columns
   *
   * @since 1.0.0
   */
  fun <E : Any> persist(entity: E): InsertMoreStep<E> {
    val details = Introspector.analyze(entity::class as KClass<E>)
    val tableInfo = TableInfo(details)
    val insertableColumns = details.insertableColumns
    val insert = insertInto(tableInfo)
    for (updatableColumn in insertableColumns) {
      insert.set(updatableColumn.asFieldOf(tableInfo), updatableColumn.getValue(entity))
    }
    return insert as InsertMoreStep<E>
  }

  /**
   * This method constructs a query like:
   * ```sql
   * INSERT INTO table (c1, c2, c3)
   * VALUES (?, ?, ?), (?, ?, ?),...
   * ```
   *
   * @since 1.0.0
   * @see persist
   */
  fun <E : Any> persistMany(entities: Collection<E>): InsertValuesStep<E> {
    val details = Introspector.analyze(entities.first()::class as KClass<E>)
    val tableInfo = TableInfo(details)
    val insertableColumns = details.insertableColumns
    val insert = insertInto(tableInfo).columns(*insertableColumns.map { it.asFieldOf(tableInfo) }.toTypedArray())
    for (entity in entities) {
      insert.values(*insertableColumns.map { it.getValue(entity) }.toTypedArray())
    }
    return insert
  }

  companion object {
    /**
     * Construct a repository using the specified JdbcTemplate
     */
    fun using(template: JdbcTemplate) = DefaultRepository(template)
  }
}

