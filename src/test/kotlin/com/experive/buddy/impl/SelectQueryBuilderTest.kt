package com.experive.buddy.impl

import com.experive.buddy.*
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class SelectQueryBuilderTest {

  @ParameterizedTest
  @MethodSource("queries")
  fun testQueryGeneration(queryBuilder: SelectQueryBuilder<TestEntity>, expectedQuery: String) {
    Assertions.assertThat(queryBuilder.toSQL()).isEqualTo(expectedQuery)
  }

  companion object {
    fun qb(vararg selectFieldOrAsterisk: Expression<*>) = SelectQueryBuilder(mockk(), TestEntity::class.java, *selectFieldOrAsterisk).from(table)

    @JvmStatic
    fun queries(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(qb().join(testRelationTable, trTestId), "SELECT ${table.alias}.* FROM test_entity ${table.alias} JOIN test_relation ${testRelationTable.alias} ON ${table.alias}.id = ${testRelationTable.alias}.test_id"),
        Arguments.of(qb().leftJoin(testRelationTable, trTestId), "SELECT ${table.alias}.* FROM test_entity ${table.alias} LEFT JOIN test_relation ${testRelationTable.alias} ON ${table.alias}.id = ${testRelationTable.alias}.test_id"),
        Arguments.of(qb().rightJoin(testRelationTable, trTestId), "SELECT ${table.alias}.* FROM test_entity ${table.alias} RIGHT JOIN test_relation ${testRelationTable.alias} ON ${table.alias}.id = ${testRelationTable.alias}.test_id"),
        Arguments.of(qb(), "SELECT ${table.alias}.* FROM test_entity ${table.alias}"),
        Arguments.of(qb().where(name.eq("anything")), "SELECT ${table.alias}.* FROM test_entity ${table.alias} WHERE ${table.alias}.name = ?"),
        Arguments.of(qb().limit(5), "SELECT ${table.alias}.* FROM test_entity ${table.alias} LIMIT 5"),
        Arguments.of(qb().limit(5).offset(7), "SELECT ${table.alias}.* FROM test_entity ${table.alias} LIMIT 5 OFFSET 7"),
        Arguments.of(qb(name, count()).groupBy(name), "SELECT ${table.alias}.name, count(*) FROM test_entity ${table.alias} GROUP BY ${table.alias}.name"),
        Arguments.of(qb(name, count()).groupBy(name).having(count().greaterOrEqual(2)), "SELECT ${table.alias}.name, count(*) FROM test_entity ${table.alias} GROUP BY ${table.alias}.name HAVING count(*) >= ?"),
        Arguments.of(qb().orderBy(name), "SELECT ${table.alias}.* FROM test_entity ${table.alias} ORDER BY ${table.alias}.name ASC NULLS LAST"),
        Arguments.of(qb().orderBy(name, Direction.DESC), "SELECT ${table.alias}.* FROM test_entity ${table.alias} ORDER BY ${table.alias}.name DESC NULLS LAST"),

        )
    }

    @JvmStatic
    private val table = TestEntity::class.java.table()

    @JvmStatic
    private val testRelationTable = TestRelation::class.java.table()

    @JvmStatic
    private val trTestId = testRelationTable.column(TestRelation::testId)

    @JvmStatic
    private val name = table.column(TestEntity::name)
  }
}