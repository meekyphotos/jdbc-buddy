package com.experive.buddy.impl

import com.experive.buddy.Table
import com.experive.buddy.predicates.Predicate

internal data class JoinDetails(val joinType: JoinType, val table: Table<*>, val predicate: Predicate)