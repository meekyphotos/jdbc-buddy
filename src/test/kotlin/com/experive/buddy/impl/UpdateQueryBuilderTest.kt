package com.experive.buddy.impl

import com.experive.buddy.TestEntity
import com.experive.buddy.table
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UpdateQueryBuilderTest {
  @Test
  internal fun shouldCheckIfColumnsToUpdateAreEmpty() {
    val table = TestEntity::class.table()
    val underTest = UpdateQueryBuilder(table, mockk())

    val exception = assertThrows<IllegalStateException> {
      underTest.toSQL()
    }
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().isEqualTo("You need to specify at least one column to update")
  }

  @Test
  internal fun shouldNotAddWhereConditionWhenPredicatesAreEmpty() {
    val table = TestEntity::class.table()
    val name = table.column(TestEntity::name)
    val underTest = UpdateQueryBuilder(table, mockk())

    underTest.set(name, "")

    assertThat(
      underTest.toSQL()
    ).isEqualTo("UPDATE test_entity ${table.alias} SET name=?")
  }

}