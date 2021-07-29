package com.experive.buddy.impl

import com.experive.buddy.Table
import com.experive.buddy.TableField
import com.experive.buddy.predicates.Predicate
import com.experive.buddy.steps.SelectJoinStep
import com.experive.buddy.steps.SelectOnStep

internal class SelectOnBuilder<R, Q>(
  private val joinType: JoinType,
  private val parent: SelectQueryBuilder<R>,
  private val table: Table<Q>
) : SelectOnStep<R, Q> {
  override fun on(predicate: Predicate): SelectJoinStep<R> {
    parent.joins.add(JoinDetails(joinType, table, predicate))
    return parent
  }

  override fun <X> on(rTableField: TableField<R, X>, qTableField: TableField<Q, X>): SelectJoinStep<R> {
    val predicate = rTableField.eq(qTableField)
    parent.joins.add(JoinDetails(joinType, table, predicate))
    return parent
  }
}