package com.experive.buddy.steps

import com.experive.buddy.Expression
import com.experive.buddy.TableField

interface InsertValuesStep<R> : InsertOnDuplicateStep<R> {
  fun values(vararg values: Any?): InsertValuesStep<R>
  fun select(selectQuery: Select<R>): InsertOnDuplicateStep<R>
}

interface InsertOnDuplicateStep<R> : InsertReturningStep<R> {
  fun onConflictDoNothing(): InsertReturningStep<R>
  fun onDuplicateKeyIgnore(): InsertReturningStep<R>
}

interface Query {
  fun execute(): Int
  fun toSQL(): String
}

interface Insert<R> : Query

interface InsertResultStep<R> : Insert<R>, FetchableQuery<R>

interface InsertReturningStep<R> : Insert<R> {
  fun returning(): InsertResultStep<R>
  fun returning(vararg selectFieldOrAsterisk: Expression<*>): InsertResultStep<R>
}

interface InsertMoreStep<R> : InsertOnDuplicateStep<R> {
  fun <T> set(tableField: TableField<R, T>, value: T): InsertMoreStep<R>
}

interface InsertSetStep<R> {
  fun columns(vararg tableFields: TableField<R, *>): InsertValuesStep<R>
  fun <T> set(tableField: TableField<R, T>, value: T): InsertMoreStep<R>
}