package com.experive.buddy.impl

import com.experive.buddy.Direction
import com.experive.buddy.Expression
import com.experive.buddy.TestEntity
import com.experive.buddy.TestRelation
import com.experive.buddy.count
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.greaterOrEqual
import com.experive.buddy.steps.SelectJoinStep
import com.experive.buddy.table
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class SelectQueryBuilderTest {

    @ParameterizedTest
    @MethodSource("queries")
    fun testQueryGeneration(queryBuilder: SelectQueryBuilder<TestEntity>, expectedQuery: String) {
        assertThat(queryBuilder.toSQL()).isEqualTo(expectedQuery)
    }

    companion object {
        private fun qb(vararg selectFieldOrAsterisk: Expression<*>): SelectJoinStep<TestEntity> {
            return SelectQueryBuilder(mockk(), TestEntity::class, Dialect.of(""), *selectFieldOrAsterisk).from(table)
        }

        @JvmStatic
        @SuppressWarnings("LongMethod") // there are many queries to test
        fun queries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    qb().join(testRelationTable, trTestId),
                    "SELECT ${table.alias}.* " +
                        "FROM test_entity ${table.alias} " +
                        "JOIN test_relation ${testRelationTable.alias} " +
                        "ON ${table.alias}.id = ${testRelationTable.alias}.test_id"
                ),
                Arguments.of(
                    qb().leftJoin(testRelationTable, trTestId),
                    "SELECT ${table.alias}.* " +
                        "FROM test_entity ${table.alias} " +
                        "LEFT JOIN test_relation ${testRelationTable.alias} " +
                        "ON ${table.alias}.id = ${testRelationTable.alias}.test_id"
                ),
                Arguments.of(
                    qb().rightJoin(testRelationTable, trTestId),
                    "SELECT ${table.alias}.* " +
                        "FROM test_entity ${table.alias} " +
                        "RIGHT JOIN test_relation ${testRelationTable.alias} " +
                        "ON ${table.alias}.id = ${testRelationTable.alias}.test_id"
                ),
                Arguments.of(
                    qb(),
                    "SELECT ${table.alias}.* FROM test_entity ${table.alias}"
                ),
                Arguments.of(
                    qb().where(name.eq("anything")),
                    "SELECT ${table.alias}.* FROM test_entity ${table.alias} WHERE ${table.alias}.name = ?"
                ),
                Arguments.of(
                    qb().limit(5),
                    "SELECT ${table.alias}.* FROM test_entity ${table.alias} LIMIT 5"
                ),
                Arguments.of(
                    qb().limit(5).offset(7),
                    "SELECT ${table.alias}.* FROM test_entity ${table.alias} LIMIT 5 OFFSET 7"
                ),
                Arguments.of(
                    qb(name, count()).groupBy(name),
                    "SELECT ${table.alias}.name, count(*) FROM test_entity ${table.alias} GROUP BY ${table.alias}.name"
                ),
                Arguments.of(
                    qb(name, count()).groupBy(name).having(count().greaterOrEqual(2)),
                    "SELECT ${table.alias}.name, count(*) " +
                        "FROM test_entity ${table.alias} GROUP BY ${table.alias}.name HAVING count(*) >= ?"
                ),
                Arguments.of(
                    qb().orderBy(name),
                    "SELECT ${table.alias}.* " +
                        "FROM test_entity ${table.alias} ORDER BY ${table.alias}.name ASC NULLS LAST"
                ),
                Arguments.of(
                    qb().orderBy(name, Direction.DESC),
                    "SELECT ${table.alias}.* " +
                        "FROM test_entity ${table.alias} ORDER BY ${table.alias}.name DESC NULLS LAST"
                ),

            )
        }

        @JvmStatic
        private val table = TestEntity::class.table()

        @JvmStatic
        private val testRelationTable = TestRelation::class.table()

        @JvmStatic
        private val trTestId = testRelationTable.column(TestRelation::testId)

        @JvmStatic
        private val name = table.column(TestEntity::name)
    }
}
