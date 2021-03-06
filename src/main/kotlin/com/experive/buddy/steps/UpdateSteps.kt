package com.experive.buddy.steps

import com.experive.buddy.TableField
import com.experive.buddy.predicates.Predicate

interface UpdateSetMoreStep<R : Any> : UpdateSetStep<R>, UpdateWhereStep<R>

interface Update<R> : Query

interface UpdateWhereStep<R> {
    fun where(vararg predicates: Predicate): Update<R>
}

interface UpdateSetStep<R : Any> {
    fun <Q> set(tableField: TableField<R, Q>, value: Q): UpdateSetMoreStep<R>
}
