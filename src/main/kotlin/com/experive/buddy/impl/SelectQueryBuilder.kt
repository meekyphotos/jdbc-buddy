package com.experive.buddy.impl

import com.experive.buddy.*
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.impl.results.StreamQueryResult
import com.experive.buddy.predicates.Predicate
import com.experive.buddy.steps.*
import org.springframework.jdbc.core.JdbcTemplate

internal class SelectQueryBuilder<R>(
  private val template: JdbcTemplate,
  private val recordClass: Class<R>,
  private val dialect: Dialect,
  private vararg val selectFieldOrAsterisk: Expression<*>
) : SelectFromStep<R>, SelectWhereStep<R>, SelectOffsetStep<R>, SelectJoinStep<R> {
  internal val joins = ArrayList<JoinDetails>()
  private lateinit var root: Table<*>

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

  override fun <Q> join(otherTable: Table<Q>): SelectOnStep<R, Q> {
    return SelectOnBuilder(JoinType.JOIN, this, otherTable)
  }

  override fun <Q> join(otherTable: Table<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R> {
    return join(otherTable).on(root.idColumn<Any>()!!.eq(qTableField))
  }

  override fun <Q> leftJoin(otherTable: Table<Q>): SelectOnStep<R, Q> {
    return SelectOnBuilder(JoinType.LEFT, this, otherTable)
  }

  override fun <Q> leftJoin(otherTable: Table<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R> {
    return leftJoin(otherTable).on(root.idColumn<Any>()!!.eq(qTableField))
  }

  override fun <Q> rightJoin(otherTable: Table<Q>): SelectOnStep<R, Q> {
    return SelectOnBuilder(JoinType.RIGHT, this, otherTable)
  }

  override fun <Q> rightJoin(otherTable: Table<Q>, qTableField: TableField<Q, *>): SelectJoinStep<R> {
    return rightJoin(otherTable).on(root.idColumn<Any>()!!.eq(qTableField))
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

  override fun fetchInto(): QueryResult<R> = fetch().map { it.into(recordClass) }

  override fun fetchOneInto(): R? = fetchOne()?.into(recordClass)

  override fun fetchSingleInto(): R = fetchSingle().into(recordClass)

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
        } + it.table.name() + " " + it.table.alias + " ON " + it.predicate.toQualifiedSqlFragment()
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

  override fun from(table: Table<*>): SelectJoinStep<R> {
    this.root = table
    return this
  }

}