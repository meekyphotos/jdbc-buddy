package com.experive.buddy.impl

import com.experive.buddy.TableField
import com.experive.buddy.TableInfo
import com.experive.buddy.predicates.Predicate
import com.experive.buddy.steps.SelectJoinStep
import com.experive.buddy.steps.SelectOnStep

internal class SelectOnBuilder<R : Any, Q : Any>(
    private val joinType: JoinType,
    private val parent: SelectQueryBuilder<R>,
    private val tableInfo: TableInfo<Q>
) : SelectOnStep<R, Q> {
    override fun on(predicate: Predicate): SelectJoinStep<R> {
        parent.joins.add(JoinDetails(joinType, tableInfo, predicate))
        return parent
    }

    override fun <X> on(rTableField: TableField<R, X>, qTableField: TableField<Q, X>): SelectJoinStep<R> {
        val predicate = rTableField.eq(qTableField)
        parent.joins.add(JoinDetails(joinType, tableInfo, predicate))
        return parent
    }
}
