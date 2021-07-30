package com.experive.buddy

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class TableTest {

  @Test
  @DisplayName("should return null when requesting a column that doesn't exist")
  internal fun shouldReturnNullWhenRequestingAnUnknownColumn() {
    val table = TestEntity::class.java.table()
    val column = table.column<String?>("pippo")
    assertThat(column).isNull()
  }

  @Test
  @DisplayName("should return null when requesting the id column when id doesn't exist")
  internal fun shouldReturnNullWhenRequestingIdOnTableWithoutId() {
    val table = AliasedTestEntity::class.java.table()
    val column = table.idColumn<Long>()
    assertThat(column).isNull()
  }

  @Test
  @DisplayName("should return column when exist")
  internal fun shouldReturnWhenColumnExists() {
    val table = TestEntity::class.java.table()
    val column = table.column(TestEntity::name)
    assertThat(column).isNotNull()
    assertThat(column.name).isEqualTo("name")
  }

  @Test
  @DisplayName("should return id column")
  internal fun shouldReturnWhenRequestingId() {
    val table = TestEntity::class.java.table()
    val column = table.idColumn<Int>()
    assertThat(column).isNotNull()
    assertThat(column!!.name).isEqualTo("id")
  }

}