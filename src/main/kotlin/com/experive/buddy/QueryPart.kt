package com.experive.buddy

interface QueryPart {
  fun toQualifiedSqlFragment(): String
  fun toSqlFragment(): String = toQualifiedSqlFragment()
}