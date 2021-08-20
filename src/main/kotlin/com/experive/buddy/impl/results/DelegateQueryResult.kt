package com.experive.buddy.impl.results

import com.experive.buddy.QueryResult
import java.util.function.Function

class DelegateQueryResult<I, O>(private val delegate: QueryResult<I>, private val mapper: Function<in I, out O>) : QueryResult<O> {
    override fun toList(): List<O> {
        return delegate.toList().map { e -> mapper.apply(e) }
    }

    override fun next(): O = delegate.next().let { mapper.apply(it) }

    override fun hasNext(): Boolean = delegate.hasNext()
}
