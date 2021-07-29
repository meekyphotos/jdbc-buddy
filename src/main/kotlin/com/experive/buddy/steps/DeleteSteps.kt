package com.experive.buddy.steps

import com.experive.buddy.predicates.Predicate

interface Delete<R> : Query
interface DeleteWhereStep<R> : Delete<R> {
  fun where(vararg predicates: Predicate): Delete<R>
}