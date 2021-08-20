package com.experive.buddy.steps

import com.experive.buddy.Expression
import com.experive.buddy.TableField

interface InsertValuesStep<R : Any> : InsertOnDuplicateStep<R> {
    fun values(vararg values: Any?): InsertValuesStep<R>
    fun select(selectQuery: Select<R>): InsertOnDuplicateStep<R>
}

interface InsertOnDuplicateStep<R : Any> : InsertReturningStep<R> {
    fun onConflictDoNothing(): InsertReturningStep<R>
    fun onDuplicateKeyIgnore(): InsertReturningStep<R>
}

interface Query {
    fun execute(): Int
    fun toSQL(): String
}

interface Insert<R : Any> : Query

interface InsertResultStep<R : Any> : Insert<R>, FetchableQuery<R>

interface InsertReturningStep<R : Any> : Insert<R> {
    fun returning(): InsertResultStep<R>
    fun returning(vararg selectFieldOrAsterisk: Expression<*>): InsertResultStep<R>
}

interface InsertMoreStep<R : Any> : InsertOnDuplicateStep<R> {
    fun <T> set(tableField: TableField<R, T>, value: T): InsertMoreStep<R>
}

interface InsertSetStep<R : Any> {
    fun columns(vararg tableFields: TableField<R, *>): InsertValuesStep<R>
    fun <T> set(tableField: TableField<R, T>, value: T): InsertMoreStep<R>
}
