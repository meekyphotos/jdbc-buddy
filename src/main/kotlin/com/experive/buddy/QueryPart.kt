package com.experive.buddy

interface QueryPart {
  fun toSqlFragment(): String
}