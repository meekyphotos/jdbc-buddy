package com.experive.buddy.impl.results

import com.experive.buddy.QueryResult

class SafeQueryResult<E>(private val results: List<E>) : QueryResult<E> {
  private val iterator = lazy { results.iterator() }
  override fun toList(): List<E> {
    return if (this.iterator.isInitialized()) {
      super.toList()
    } else {
      results
    }
  }

  override fun next(): E = iterator.value.next()

  override fun hasNext(): Boolean = iterator.value.hasNext()

}