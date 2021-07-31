package com.experive.buddy.impl

import com.experive.buddy.TableInfo
import com.experive.buddy.predicates.Predicate

internal data class JoinDetails(val joinType: JoinType, val tableInfo: TableInfo<*>, val predicate: Predicate)