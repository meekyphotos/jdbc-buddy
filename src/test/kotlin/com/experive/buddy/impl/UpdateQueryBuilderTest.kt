package com.experive.buddy.impl

import com.experive.buddy.TestEntity
import com.experive.buddy.table
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class UpdateQueryBuilderTest {
  @Test
  internal fun shouldCheckIfColumnsToUpdateAreEmpty() {
    val table = TestEntity::class.java.table()
    val underTest = UpdateQueryBuilder(table, mockk())

    Assertions.assertThatThrownBy {
      underTest.toSQL()
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("You need to specify at least one column to update")

  }

  @Test
  internal fun shouldNotAddWhereConditionWhenPredicatesAreEmpty() {
    val table = TestEntity::class.java.table()
    val name = table.column(TestEntity::name)
    val underTest = UpdateQueryBuilder(table, mockk())

    underTest.set(name, "")

    Assertions.assertThat(
      underTest.toSQL()
    ).isEqualTo("UPDATE test_entity ${table.alias} SET name=?")
  }

}