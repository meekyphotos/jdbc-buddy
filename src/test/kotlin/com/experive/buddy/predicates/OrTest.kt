package com.experive.buddy.predicates

import com.experive.buddy.TestEntity
import com.experive.buddy.table
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class OrTest {
  @Test
  @DisplayName("Or: should throw when no elements are provided")
  internal fun noElements() {
    val e = assertThrows<IllegalStateException> { Or() }
    assertThat(e).hasMessageThat().isEqualTo("Or requires at least one condition")
  }

  @Test
  @DisplayName("Or: prints the predicate when there is only 1 predicate")
  internal fun printsPredicate() {
    val table = TestEntity::class.table()
    val field = table.column(TestEntity::name)
    val underTest = Or(field eq "hello")

    assertThat(underTest.toSqlFragment()).isEqualTo("${table.alias}.name = ?")
    assertThat(underTest.collectValues()).isEqualTo(arrayListOf("hello"))
  }

  @Test
  @DisplayName("Or: prints all the predicates separated by or")
  internal fun printsAllPredicates() {
    val table = TestEntity::class.table()
    val field = table.column(TestEntity::name)
    val underTest = Or(field eq "hello", field notEqual "world")

    assertThat(underTest.toSqlFragment()).isEqualTo("(${table.alias}.name = ? or ${table.alias}.name <> ?)")
    assertThat(underTest.collectValues()).isEqualTo(arrayListOf("hello", "world"))
  }

}