package com.experive.buddy.impl.results

import com.experive.buddy.QueryResult
import java.util.stream.Collectors
import java.util.stream.Stream

class StreamQueryResult<E>(private val results: Stream<E>) : QueryResult<E> {
  private val iterator = lazy { results.iterator() }
  override fun toList(): List<E> = results.collect(Collectors.toList())

  override fun next(): E = iterator.value.next()

  override fun hasNext(): Boolean = iterator.value.hasNext()

}