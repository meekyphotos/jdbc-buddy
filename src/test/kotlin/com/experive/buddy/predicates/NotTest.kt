package com.experive.buddy.predicates

import com.experive.buddy.TestEntity
import com.experive.buddy.table
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class NotTest {
  @Test
  @DisplayName("Not: prints the predicate")
  internal fun printsPredicate() {
    val table = TestEntity::class.table()
    val field = table.column(TestEntity::name)
    val underTest = Not(field eq "hello")

    assertThat(underTest.toSqlFragment()).isEqualTo("not (${table.alias}.name = ?)")
    assertThat(underTest.collectValues()).isEqualTo(arrayListOf("hello"))
  }


}