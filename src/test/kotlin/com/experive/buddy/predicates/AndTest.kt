package com.experive.buddy.predicates

import com.experive.buddy.TestEntity
import com.experive.buddy.table
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AndTest {
    @Test
    @DisplayName("And: should throw when no elements are provided")
    internal fun noElements() {
        val e = assertThrows<IllegalStateException> { And() }
        assertThat(e).hasMessageThat().isEqualTo("And requires at least one condition")
    }

    @Test
    @DisplayName("And: prints the predicate when there is only 1 predicate")
    internal fun printsPredicate() {
        val table = TestEntity::class.table()
        val field = table.column(TestEntity::name)
        val and = And(field eq "hello")

        assertThat(and.toSqlFragment()).isEqualTo("${table.alias}.name = ?")
        assertThat(and.collectValues()).isEqualTo(arrayListOf("hello"))
    }

    @Test
    @DisplayName("And: prints all the predicates separated by AND")
    internal fun printsAllPredicates() {
        val table = TestEntity::class.table()
        val field = table.column(TestEntity::name)
        val and = And(field eq "hello", field notEqual "world")

        assertThat(and.toSqlFragment()).isEqualTo("${table.alias}.name = ? and ${table.alias}.name <> ?")
        assertThat(and.collectValues()).isEqualTo(arrayListOf("hello", "world"))
    }
}
