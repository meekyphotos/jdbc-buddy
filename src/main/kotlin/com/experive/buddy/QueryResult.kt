package com.experive.buddy

import com.experive.buddy.impl.results.DelegateQueryResult
import java.util.function.Function

/**
 * Represents a query result
 *
 * There are two main implementations provided by this library:
 * 1. [com.experive.buddy.impl.results.StreamQueryResult] leverages the iterator like interface provided here to stream results
 * 2. [com.experive.buddy.impl.results.SafeQueryResult] reads all the result in-memory and then provides an iterator or quick access to the list
 *
 * Select queries uses the first implementation to handle large amount of data being returned and processed,
 * while insert with returning since it usually involves few elements being returned
 *
 * @see com.experive.buddy.impl.results.StreamQueryResult
 * @see com.experive.buddy.impl.results.SafeQueryResult
 */
interface QueryResult<E> {
    /**
     * Collects the remaining items into a list
     */
    fun toList(): List<E> {
        val e = ArrayList<E>()
        while (hasNext()) {
            e.add(next())
        }
        return e
    }

    /**
     * Returns the next element in the iteration.
     */
    operator fun next(): E

    /**
     * Returns `true` if the iteration has more elements.
     */
    operator fun hasNext(): Boolean

    /**
     * Return a new query result in which elements returned by [next] are mapped by the [mapper] function.
     *
     * Items already returned by [next] won't be re-processed
     */
    fun <R> map(mapper: Function<in E, out R>): QueryResult<R> = DelegateQueryResult(this, mapper)
}
