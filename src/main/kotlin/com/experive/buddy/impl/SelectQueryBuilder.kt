package com.experive.buddy.impl

import com.experive.buddy.Direction
import com.experive.buddy.Expression
import com.experive.buddy.Order
import com.experive.buddy.QueryResult
import com.experive.buddy.Record
import com.experive.buddy.TableField
import com.experive.buddy.TableInfo
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.predicates.Predicate
import com.experive.buddy.steps.Select
import com.experive.buddy.steps.SelectFromStep
import com.experive.buddy.steps.SelectGroupByStep
import com.experive.buddy.steps.SelectHavingStep
import com.experive.buddy.steps.SelectJoinStep
import com.experive.buddy.steps.SelectLimitStep
import com.experive.buddy.steps.SelectOffsetStep
import com.experive.buddy.steps.SelectOnStep
import com.experive.buddy.steps.SelectOrderByStep
import com.experive.buddy.steps.SelectWhereStep
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.reflect.KClass

internal class SelectQueryBuilder<R : Any>(
    private val template: JdbcTemplate,
    private val recordClass: KClass<R>,
    private val dialect: Dialect,
    private vararg val selectFieldOrAsterisk: Expression<*>
) : SelectFromStep<R>, SelectWhereStep<R>, SelectOffsetStep<R>, SelectJoinStep<R> {
    internal val joins = ArrayList<JoinDetails>()
    private lateinit var root: TableInfo<*>

    private var offset: Int? = null
    private var limit: Int? = null
    private var orderBy = ArrayList<Order>()
    private var groupBy = ArrayList<TableField<*, *>>()
    private var havingPredicates = ArrayList<Predicate>()
    private var predicates = ArrayList<Predicate>()

    override fun iterator(): Iterator<R> {
        return object : Iterator<R> {
            val seq = fetchInto()
            override fun hasNext(): Boolean = seq.hasNext()

            override fun next(): R = seq.next()
        }
    }

    override fun <Q : Any> join(otherTableInfo: TableInfo<Q>): SelectOnStep<R, Q> {
        return SelectOnBuilder(JoinType.JOIN, this, otherTableInfo)
    }

    override fun <Q : Any> join(otherTableInfo: TableInfo<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R> {
        return join(otherTableInfo).on(root.idColumn<Any>()!!.eq(qTableField))
    }

    override fun <Q : Any> leftJoin(otherTableInfo: TableInfo<Q>): SelectOnStep<R, Q> {
        return SelectOnBuilder(JoinType.LEFT, this, otherTableInfo)
    }

    override fun <Q : Any> leftJoin(otherTableInfo: TableInfo<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R> {
        return leftJoin(otherTableInfo).on(root.idColumn<Any>()!!.eq(qTableField))
    }

    override fun <Q : Any> rightJoin(otherTableInfo: TableInfo<Q>): SelectOnStep<R, Q> {
        return SelectOnBuilder(JoinType.RIGHT, this, otherTableInfo)
    }

    override fun <Q : Any> rightJoin(otherTableInfo: TableInfo<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R> {
        return rightJoin(otherTableInfo).on(root.idColumn<Any>()!!.eq(qTableField))
    }

    override fun offset(amount: Int): Select<R> {
        this.offset = amount
        return this
    }

    override fun limit(amount: Int): SelectOffsetStep<R> {
        this.limit = amount
        return this
    }

    override fun orderBy(vararg tableField: TableField<*, *>): SelectLimitStep<R> {
        this.orderBy.addAll(tableField.map { Order(it, Direction.ASC) })
        return this
    }

    override fun orderBy(tableField: TableField<*, *>, direction: Direction): SelectOrderByStep<R> {
        this.orderBy.add(Order(tableField, direction))
        return this
    }

    override fun having(vararg predicate: Predicate): SelectOrderByStep<R> {
        havingPredicates.addAll(predicate)
        return this
    }

    override fun groupBy(vararg tableField: TableField<*, *>): SelectHavingStep<R> {
        groupBy.addAll(tableField)
        return this
    }

    override fun where(vararg predicate: Predicate): SelectGroupByStep<R> {
        predicates.addAll(predicate)
        return this
    }

    override fun fetch(): QueryResult<Record> {
        return StreamQueryResult(template.queryForStream(toSQL(), RecordMapper(dialect), *collectParameters().toTypedArray()))
    }

    override fun fetchInto(): QueryResult<R> = fetch().map { it.into(recordClass)!! }

    override fun fetchOneInto(): R? = fetchOne()?.into(recordClass)

    override fun fetchSingleInto(): R = fetchSingle().into(recordClass)!!

    override fun toSQL(): String {
        val hasJoins = joins.isNotEmpty()
        val sb = StringBuilder()
        sb.append("SELECT ")
        if (selectFieldOrAsterisk.isEmpty()) {
            sb.append(root.alias + ".*")
        } else {
            selectFieldOrAsterisk.joinTo(sb, ", ") { it.toQualifiedSqlFragment() }
        }
        sb.append(" FROM ${root.name()} " + root.alias)
        if (hasJoins) {
            sb.append(" ")
            joins.joinTo(sb, " ") {
                when (it.joinType) {
                    JoinType.JOIN -> "JOIN "
                    JoinType.LEFT -> "LEFT JOIN "
                    JoinType.RIGHT -> "RIGHT JOIN "
                } + it.tableInfo.name() + " " + it.tableInfo.alias + " ON " + it.predicate.toQualifiedSqlFragment()
            }
        }
        if (predicates.isNotEmpty()) {
            sb.append(" WHERE ")
            predicates.joinTo(sb, " and ") { it.toQualifiedSqlFragment() }
        }
        if (groupBy.isNotEmpty()) {
            sb.append(" GROUP BY ")
            groupBy.joinTo(sb, ", ") { it.toQualifiedSqlFragment() }
        }
        if (havingPredicates.isNotEmpty()) {
            sb.append(" HAVING ")
            havingPredicates.joinTo(sb, " and ") { it.toQualifiedSqlFragment() }
        }
        if (orderBy.isNotEmpty()) {
            sb.append(" ORDER BY ")
            orderBy.joinTo(sb, ", ") { it.toQualifiedSqlFragment() }
        }
        if (limit != null) {
            sb.append(" LIMIT $limit")
        }
        if (offset != null) {
            sb.append(" OFFSET $offset")
        }
        return sb.toString()
    }

    override fun collectParameters(): List<Any?> {
        val out = ArrayList<Any?>()
        out.addAll(selectFieldOrAsterisk.flatMap { it.collectValues() })
        out.addAll(predicates.flatMap { it.collectValues() })
        out.addAll(groupBy.flatMap { it.collectValues() })
        out.addAll(havingPredicates.flatMap { it.collectValues() })
        return out
    }

    override fun from(tableInfo: TableInfo<*>): SelectJoinStep<R> {
        this.root = tableInfo
        return this
    }
}
