package com.experive.buddy.impl

import com.experive.buddy.TestEntity
import com.experive.buddy.table
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class DeleteQueryBuilderTest {
  @Test
  internal fun shouldNotAddWhereConditionWhenPredicatesAreEmpty() {
    val table = TestEntity::class.java.table()
    val underTest = DeleteQueryBuilder(table, mockk())
    assertThat(
      underTest.toSQL()
    ).isEqualTo("DELETE FROM test_entity ${table.alias}")
  }

  @Test
  internal fun shouldAddWhereConditionWhenPredicatesAreProvided() {
    val table = TestEntity::class.java.table()
    val name = table.column(TestEntity::name)
    val underTest = DeleteQueryBuilder(table, mockk())

    underTest.where(name.eq("something"))

    assertThat(
      underTest.toSQL()
    ).isEqualTo("DELETE FROM test_entity ${table.alias} WHERE ${table.alias}.name = ?")
  }

}