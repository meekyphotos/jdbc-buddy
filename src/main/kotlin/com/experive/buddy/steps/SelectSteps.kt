package com.experive.buddy.steps

import com.experive.buddy.*
import com.experive.buddy.exceptions.NoDataFoundException
import com.experive.buddy.exceptions.TooManyRowsException
import com.experive.buddy.predicates.Predicate
import org.springframework.dao.DataAccessException

interface SelectOnStep<R, Q> {
  fun on(predicate: Predicate): SelectJoinStep<R>
  fun <X> on(rTableField: TableField<R, X>, qTableField: TableField<Q, X>): SelectJoinStep<R>
}

interface SelectJoinStep<R> : SelectWhereStep<R> {
  fun <Q> join(otherTable: Table<Q>): SelectOnStep<R, Q>
  fun <Q> join(otherTable: Table<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R>

  fun <Q> leftJoin(otherTable: Table<Q>): SelectOnStep<R, Q>
  fun <Q> leftJoin(otherTable: Table<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R>

  fun <Q> rightJoin(otherTable: Table<Q>): SelectOnStep<R, Q>
  fun <Q> rightJoin(otherTable: Table<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R>

}

interface SelectFromStep<R> {
  fun from(table: Table<R>): SelectJoinStep<R>
}

interface SelectOffsetStep<R> : Select<R> {
  fun offset(amount: Int): Select<R>
}

interface SelectLimitStep<R> : Select<R> {
  fun limit(amount: Int): SelectOffsetStep<R>
}

interface SelectOrderByStep<R> : SelectLimitStep<R> {
  fun orderBy(vararg tableField: TableField<*, *>): SelectLimitStep<R>
  fun orderBy(tableField: TableField<*, *>, direction: Direction): SelectOrderByStep<R>
}

interface SelectHavingStep<R> : SelectOrderByStep<R> {
  fun having(vararg predicate: Predicate): SelectOrderByStep<R>
}

interface SelectGroupByStep<R> : SelectHavingStep<R> {
  fun groupBy(vararg tableField: TableField<*, *>): SelectHavingStep<R>
}

interface SelectWhereStep<R> : SelectGroupByStep<R> {
  fun where(vararg predicate: Predicate): SelectGroupByStep<R>
}

interface FetchableQuery<R> {
  @Throws(DataAccessException::class)
  fun fetch(): QueryResult<Record>

  @Throws(DataAccessException::class)
  fun fetchInto(): QueryResult<R>

  @Throws(DataAccessException::class)
  fun <Q> fetchInto(clazz: Class<Q>): QueryResult<Q> = fetch().map { it.into(clazz) }

  @Throws(DataAccessException::class)
  fun fetchOne(): Record? {
    val fetch = fetch()
    if (fetch.hasNext()) {
      return fetch.next()
    }
    return null
  }

  @Throws(DataAccessException::class)
  fun fetchOneInto(): R?

  @Throws(DataAccessException::class)
  fun <Q> fetchOneInto(clazz: Class<Q>): Q? = fetchOne()?.into(clazz)

  @Throws(DataAccessException::class, NoDataFoundException::class, TooManyRowsException::class)
  fun fetchSingle(): Record {
    val fetch = fetch()
    if (fetch.hasNext()) {
      val result = fetch.next()
      if (fetch.hasNext()) {
        throw TooManyRowsException()
      }
      return result
    }
    throw NoDataFoundException()
  }

  @Throws(DataAccessException::class, NoDataFoundException::class, TooManyRowsException::class)
  fun fetchSingleInto(): R

  @Throws(DataAccessException::class, NoDataFoundException::class, TooManyRowsException::class)
  fun <Q> fetchSingleInto(clazz: Class<Q>): Q = fetchSingle().into(clazz)
}

interface Select<R> : Iterable<R>, FetchableQuery<R> {
  fun toSQL(): String
  fun collectParameters(): List<Any?>


}