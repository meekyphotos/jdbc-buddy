package com.experive.buddy

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class OrderTest {
  @Test
  @DisplayName("It includes alias in order by clause")
  internal fun itIncludesAlias() {
    val t = TestEntity::class.table()
    val tableField = t.idColumn<Int>()!!
    val underTest = Order(tableField, Direction.ASC)
    assertThat(underTest.toSqlFragment()).isEqualTo("${t.alias}.id ASC NULLS LAST")
  }

  @Test
  @DisplayName("It correctly prints DESC in order by clause")
  internal fun itPrintsDESC() {
    val t = TestEntity::class.table()
    val tableField = t.idColumn<Int>()!!
    val underTest = Order(tableField, Direction.DESC)
    assertThat(underTest.toSqlFragment()).isEqualTo("${t.alias}.id DESC NULLS LAST")
  }

}