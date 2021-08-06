package com.experive.buddy

/**
 * Marks an object as a part of a query
 */
interface QueryPart {
  /**
   * In most of the code, this is the method used when embedding a query part inside a query.
   *
   * There are cases when syntatically the query part shouldn't be qualified, for all those cases, [toSqlFragment] is provided.
   *
   * @see toSqlFragment
   * @since 1.0.0
   */
  fun toQualifiedSqlFragment(): String

  /**
   * Generate the sql fragment
   *
   * @since 1.0.0
   */
  fun toSqlFragment(): String = toQualifiedSqlFragment()
}