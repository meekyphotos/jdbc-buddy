package com.experive.buddy.steps

import com.experive.buddy.Direction
import com.experive.buddy.QueryResult
import com.experive.buddy.Record
import com.experive.buddy.TableField
import com.experive.buddy.TableInfo
import com.experive.buddy.exceptions.NoDataFoundException
import com.experive.buddy.exceptions.TooManyRowsException
import com.experive.buddy.predicates.Predicate
import org.springframework.dao.DataAccessException
import kotlin.reflect.KClass

interface SelectOnStep<R : Any, Q : Any> {
    fun on(predicate: Predicate): SelectJoinStep<R>
    fun <X> on(rTableField: TableField<R, X>, qTableField: TableField<Q, X>): SelectJoinStep<R>
}

interface SelectJoinStep<R : Any> : SelectWhereStep<R> {
    fun <Q : Any> join(otherTableInfo: TableInfo<Q>): SelectOnStep<R, Q>
    fun <Q : Any> join(otherTableInfo: TableInfo<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R>

    fun <Q : Any> leftJoin(otherTableInfo: TableInfo<Q>): SelectOnStep<R, Q>
    fun <Q : Any> leftJoin(otherTableInfo: TableInfo<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R>

    fun <Q : Any> rightJoin(otherTableInfo: TableInfo<Q>): SelectOnStep<R, Q>
    fun <Q : Any> rightJoin(otherTableInfo: TableInfo<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R>
}

interface SelectFromStep<R : Any> {
    fun from(tableInfo: TableInfo<*>): SelectJoinStep<R>
}

interface SelectOffsetStep<R : Any> : Select<R> {
    fun offset(amount: Int): Select<R>
}

interface SelectLimitStep<R : Any> : Select<R> {
    fun limit(amount: Int): SelectOffsetStep<R>
}

interface SelectOrderByStep<R : Any> : SelectLimitStep<R> {
    fun orderBy(vararg tableField: TableField<*, *>): SelectLimitStep<R>
    fun orderBy(tableField: TableField<*, *>, direction: Direction): SelectOrderByStep<R>
}

interface SelectHavingStep<R : Any> : SelectOrderByStep<R> {
    fun having(vararg predicate: Predicate): SelectOrderByStep<R>
}

interface SelectGroupByStep<R : Any> : SelectHavingStep<R> {
    fun groupBy(vararg tableField: TableField<*, *>): SelectHavingStep<R>
}

interface SelectWhereStep<R : Any> : SelectGroupByStep<R> {
    fun where(vararg predicate: Predicate): SelectGroupByStep<R>
}

interface FetchableQuery<R : Any> {
    @Throws(DataAccessException::class)
    fun fetch(): QueryResult<Record>

    @Throws(DataAccessException::class)
    fun fetchInto(): QueryResult<R>

    @Throws(DataAccessException::class)
    fun <Q : Any> fetchInto(clazz: KClass<Q>): QueryResult<Q> = fetch().map { it.into(clazz)!! }

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
    fun <Q : Any> fetchOneInto(clazz: KClass<Q>): Q? = fetchOne()?.into(clazz)

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
    fun <Q : Any> fetchSingleInto(clazz: KClass<Q>): Q = fetchSingle().into(clazz)!!
}

interface Select<R : Any> : Iterable<R>, FetchableQuery<R> {
    fun toSQL(): String
    fun collectParameters(): List<Any?>
}
