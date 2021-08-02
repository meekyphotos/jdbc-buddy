package com.experive.buddy.impl

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.experive.buddy.*
import com.experive.buddy.dialect.Dialect
import com.experive.buddy.support.BuddyH2Extension
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.jdbc.core.JdbcTemplate
import java.util.stream.Stream

@ExtendWith(BuddyH2Extension::class)
internal class InsertQueryBuilderTest {
  lateinit var txManager: JdbcTemplate
  lateinit var underTest: Database

  @BeforeEach
  internal fun setUp() {
    underTest = Database.using(txManager)

    underTest.execute("drop table if exists test_entity")
    underTest.execute("create table if not exists test_entity (id int primary key auto_increment, name varchar unique, field_name int, boolean_field boolean)")
    underTest.execute("create table if not exists test_relation (id int primary key auto_increment, test_id int, active boolean)")
    underTest.execute("create table if not exists test_json (id int primary key auto_increment, map json, relation json)")
    underTest.persistMany(
      arrayListOf(
        TestEntity(null, "Michele", 3),
      )
    ).execute()
    underTest.persistMany(
      (0 until 5).map { TestRelation(null, it, it % 2 == 0) }
    ).execute()

  }

  @ParameterizedTest
  @MethodSource("queries")
  fun testQueryGeneration(queryBuilder: InsertQueryBuilder<TestEntity>, expectedQuery: String) {
    assertThat(queryBuilder.toSQL()).isEqualTo(expectedQuery)
  }

  @ParameterizedTest
  @MethodSource("executionQueries")
  fun testQueryExecution(create: (InsertQueryBuilder<TestEntity>) -> InsertQueryBuilder<TestEntity>, expected: Int) {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    val execute = create(base).execute()
    assertThat(execute).isEqualTo(expected)
  }

  @Test
  fun testReturningId_usingFetchOne() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOne()
    assertThat(r).isNotNull()
    assertThat(r!!.containsKey("id")).isTrue()
  }

  @Test
  fun testReturningId_usingFetchOneIntoClass() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOneInto(Integer::class)
    assertThat(r).isNotNull()
  }

  @Test
  fun testReturningId_usingFetchSingleIntoClass() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning()
    val r = base.fetchSingleInto(Integer::class)
    assertThat(r).isNotNull()
  }

  @Test
  fun testFetchShouldThrowWhenNoReturning() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1)

    assertThat(assertThrows<IllegalStateException> { base.fetchSingleInto(Integer::class) })
      .hasMessageThat()
      .isEqualTo("Fetch is only allowed when using returning")

  }

  @Test
  fun testReturningId_usingFetchOneInto() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOneInto()
    assertThat(r).isNotNull()
    assertThat(r!!.id).isNotNull()
  }

  @Test
  fun testReturningId_usingFetchSingleInto() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchSingleInto()
    assertThat(r).isNotNull()
    assertThat(r.id).isNotNull()
  }

  @Test
  fun testReturningId_usingFetchInto() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchInto().toList()
    assertThat(r).isNotNull()
    assertThat(r).hasSize(1)
    assertThat(r[0].id).isNotNull()
  }

  @Test
  fun testReturningId() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOne()
    assertThat(r).isNotNull()
    assertThat(r!!.containsKey("id")).isTrue()
  }

  @Test
  fun testReturningId_noResultsBack() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("Michele", 1).onDuplicateKeyIgnore().returning(id)
    val r = base.fetchOneInto()
    assertThat(r).isNull()
  }

  @Test
  fun testReturningMultipleFields() {
    val base = InsertQueryBuilder(table, txManager, Dialect.of("H2"))
    base.columns(name, fieldName).values("name", 1).returning(id, name, fieldName)
    val r = base.fetchOne()
    assertThat(r).isNotNull()
    assertThat(r!!.containsKey("id")).isTrue()
    assertThat(r.containsKey("name")).isTrue()
    assertThat(r.containsKey("field_name")).isTrue()
  }

  @Test
  fun shouldCheckImproperUsageOfColumnsMethod() {
    val qb = h2Qb()
    qb.set(name, "")
    val exception = assertThrows<IllegalStateException> {
      qb.columns(name, id)
    }
    assertThat(exception)
      .hasMessageThat()
      .isEqualTo("Columns are specified automatically when you use set(Field, value)")
  }

  @Test
  fun shouldThrowWhenNoColumnsHaveBeenSpecified() {
    val qb = h2Qb()
    val exception = assertThrows<IllegalStateException> {
      qb.toSQL()
    }
    assertThat(exception)
      .hasMessageThat()
      .isEqualTo("You need to defined at least one column to insert")
  }

  @Test
  fun shouldThrowWhenNoValuesHaveBeenSpecified() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    val exception = assertThrows<IllegalStateException> {
      qb.toSQL()
    }
    assertThat(exception)
      .hasMessageThat()
      .isEqualTo("Missing values to insert")
  }

  @Test
  fun shouldThrowWhenCallinValuesAfterSelect() {
    val qb = h2Qb()
    qb.select(qb())
    val exception = assertThrows<IllegalStateException> {
      qb.values(1, 2)
    }
    assertThat(exception)
      .hasMessageThat()
      .isEqualTo("Cannot specify values when using select insert")
  }

  @Test
  fun shouldThrowWhenCallingSelectAfterValues() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    qb.values(1, 2)
    val ex = assertThrows<IllegalStateException> {
      qb.select(qb())
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot use select after you've used set method")
  }

  @Test
  fun shouldThrowWhenCallingSelectAfterSet() {
    val qb = h2Qb()
    qb.set(name, "")
    val ex = assertThrows<IllegalStateException> {
      qb.select(qb())
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot use select after you've used values method")
  }

  @Test
  fun shouldThrowWhenCallingSetAfterSelect() {
    val qb = h2Qb()
    qb.select(qb())
    val ex = assertThrows<IllegalStateException> {
      qb.set(name, "")
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot use set method when using select insert")
  }

  @Test
  fun shouldThrowWhenCallingSetAfterValues() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    qb.values("name", 2)
    val ex = assertThrows<IllegalStateException> {
      qb.set(name, "")
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot use set method when using multiple records")
  }

  @Test
  fun shouldThrowWhenCallingSetMultipleTimesWithTheSameColumn() {
    val qb = h2Qb()
    qb.set(name, "1")
    val ex = assertThrows<IllegalStateException> {
      qb.set(name, "2")
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot set same field multiple times")
  }

  @Test
  fun shouldThrowWhenCallingSetAfterColumns() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    val ex = assertThrows<IllegalStateException> {
      qb.set(name, "2")
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot use set after calling columns")
  }

  @Test
  fun shouldThrowWhenCallingValuesWithInvalidNumberOfColumns() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    val ex = assertThrows<IllegalStateException> {
      qb.values("a")
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Specified values don't match the number of columns")
  }

  @Test
  fun shouldThrowWhenCallingValuesAfterCallingSet() {
    val qb = h2Qb()
    qb.set(name, "")
    val ex = assertThrows<IllegalStateException> {
      qb.values(1, 2)
    }
    assertThat(ex)
      .hasMessageThat()
      .isEqualTo("Cannot mix and match set(Field, value) with values")
  }

  @Test
  internal fun testJsonSupport_usingMap() {

    val base = InsertQueryBuilder(jsonTable, txManager, Dialect.of("H2"))
    val column = jsonTable.column(TestJson::map)
    base.columns(column)
      .values(JsonObject(mapOf("a" to "b")))
      .execute()

  }

  @Test
  internal fun testJsonSupport_usingArray() {

    val base = InsertQueryBuilder(jsonTable, txManager, Dialect.of("H2"))
    val column = jsonTable.column(TestJson::relation)
    base.columns(column)
      .values(JsonArray(1, 2, 3))
      .execute()

  }

  companion object {
    fun h2Qb() = InsertQueryBuilder(table, mockk(), Dialect.of("H2"))
    fun pgQb() = InsertQueryBuilder(table, mockk(), Dialect.of("PostgreSQL"))
    fun qb(vararg selectFieldOrAsterisk: Expression<*>) = SelectQueryBuilder(mockk(), TestEntity::class, Dialect.of(""), *selectFieldOrAsterisk).from(testRelationTable)

    @JvmStatic
    fun queries(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(h2Qb().columns(name, fieldName).values("", 1), "INSERT INTO test_entity (name, field_name) VALUES (?, ?)"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1), "INSERT INTO test_entity (name, field_name) VALUES (?, ?)"),

        Arguments.of(h2Qb().columns(name, fieldName).values("", 1).values("", 1), "INSERT INTO test_entity (name, field_name) VALUES (?, ?), (?, ?)"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1).values("", 1), "INSERT INTO test_entity (name, field_name) VALUES (?, ?), (?, ?)"),

        Arguments.of(h2Qb().columns(name, fieldName).values("", 1).returning(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?)"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1).returning(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) RETURNING *"),

        Arguments.of(h2Qb().columns(name, fieldName).values("", 1).returning(id), "INSERT INTO test_entity (name, field_name) VALUES (?, ?)"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1).returning(id), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) RETURNING id"),

        Arguments.of(h2Qb().columns(name, fieldName).values("", 1).onConflictDoNothing(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) ON CONFLICT DO NOTHING"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1).onConflictDoNothing(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) ON CONFLICT DO NOTHING"),

        Arguments.of(h2Qb().columns(name, fieldName).values("", 1).onDuplicateKeyIgnore(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) ON CONFLICT DO NOTHING"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1).onDuplicateKeyIgnore(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) ON CONFLICT DO NOTHING"),

        Arguments.of(h2Qb().columns(name, fieldName).values("", 1).onDuplicateKeyIgnore().returning(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) ON CONFLICT DO NOTHING"),
        Arguments.of(pgQb().columns(name, fieldName).values("", 1).onDuplicateKeyIgnore().returning(), "INSERT INTO test_entity (name, field_name) VALUES (?, ?) ON CONFLICT DO NOTHING RETURNING *"),

        Arguments.of(h2Qb().columns(name, fieldName).select(qb("name".asExpression(), trId)), "INSERT INTO test_entity (name, field_name) SELECT ?, ${testRelationTable.alias}.id FROM test_relation ${testRelationTable.alias}"),
        Arguments.of(pgQb().columns(name, fieldName).select(qb("name".asExpression(), trId)), "INSERT INTO test_entity (name, field_name) SELECT ?, ${testRelationTable.alias}.id FROM test_relation ${testRelationTable.alias}"),

        )
    }

    @JvmStatic
    fun executionQueries(): Stream<Arguments> {
      return Stream.of(
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("a", 1) }, 1),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("a", 1).values("b", 1) }, 2),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("c", 1).returning() }, 1),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("d", 1).returning(id) }, 1),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("e", 1).onConflictDoNothing() }, 1),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("Michele", 1).onConflictDoNothing() }, 0),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("d", 1).onDuplicateKeyIgnore() }, 1),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).values("d", 1).onDuplicateKeyIgnore().returning() }, 1),
        Arguments.of({ qb: InsertQueryBuilder<TestEntity> -> qb.columns(name, fieldName).select(qb(concat("name".asExpression(), trId cast "VARCHAR"), trId)) }, 5),
      )
    }

    @JvmStatic
    private val table = TestEntity::class.table()

    @JvmStatic
    private val jsonTable = TestJson::class.table()

    @JvmStatic
    private val testRelationTable = TestRelation::class.table()

    @JvmStatic
    private val trId = testRelationTable.column(TestRelation::id)


    @JvmStatic
    private val name = table.column(TestEntity::name)

    @JvmStatic
    private val fieldName = table.column(TestEntity::fieldName)

    @JvmStatic
    private val id = table.column(TestEntity::id)

    @JvmStatic
    private val booleanField = table.column(TestEntity::booleanField)
  }
}