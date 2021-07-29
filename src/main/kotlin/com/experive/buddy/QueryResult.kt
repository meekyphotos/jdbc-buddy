package com.experive.buddy

import com.experive.buddy.impl.results.DelegateQueryResult
import java.util.function.Function

interface QueryResult<E> {
  fun toList(): List<E>
  operator fun next(): E
  operator fun hasNext(): Boolean
  fun <R> map(mapper: Function<in E, out R>): QueryResult<R> = DelegateQueryResult(this, mapper)
}