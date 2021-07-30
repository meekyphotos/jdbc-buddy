package com.experive.buddy.impl

import com.experive.buddy.*
import com.experive.buddy.support.BuddyH2Extension
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.jdbc.core.JdbcTemplate
import java.util.stream.Stream

@ExtendWith(BuddyH2Extension::class)
internal class InsertQueryBuilderTest {
  lateinit var txManager: JdbcTemplate
  lateinit var underTest: Repository

  @BeforeEach
  internal fun setUp() {
    underTest = DefaultRepository(txManager)

    underTest.execute("drop table if exists test_entity")
    underTest.execute("create table if not exists test_entity (id int primary key auto_increment, name varchar unique, field_name int, boolean_field boolean)")
    underTest.execute("create table if not exists test_relation (id int primary key auto_increment, test_id int, active boolean)")
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
    Assertions.assertThat(queryBuilder.toSQL()).isEqualTo(expectedQuery)
  }

  @ParameterizedTest
  @MethodSource("executionQueries")
  fun testQueryExecution(create: (InsertQueryBuilder<TestEntity>) -> InsertQueryBuilder<TestEntity>, expected: Int) {
    val base = InsertQueryBuilder(table, txManager, "H2")
    val execute = create(base).execute()
    Assertions.assertThat(execute).isEqualTo(expected)
  }

  @Test
  fun testReturningId_usingFetchOne() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOne()
    Assertions.assertThat(r).isNotNull
    Assertions.assertThat(r!!.containsKey("id")).isTrue
  }

  @Test
  fun testReturningId_usingFetchOneIntoClass() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOneInto(Integer::class.java)
    Assertions.assertThat(r).isNotNull
  }

  @Test
  fun testReturningId_usingFetchSingleIntoClass() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning()
    val r = base.fetchSingleInto(Integer::class.java)
    Assertions.assertThat(r).isNotNull
  }

  @Test
  fun testFetchShouldThrowWhenNoReturning() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1)
    Assertions.assertThatThrownBy { base.fetchSingleInto(Integer::class.java) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Fetch is only allowed when using returning")

  }

  @Test
  fun testReturningId_usingFetchOneInto() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOneInto()
    Assertions.assertThat(r).isNotNull
    Assertions.assertThat(r!!.id).isNotNull
  }

  @Test
  fun testReturningId_usingFetchSingleInto() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchSingleInto()
    Assertions.assertThat(r).isNotNull
    Assertions.assertThat(r.id).isNotNull
  }

  @Test
  fun testReturningId_usingFetchInto() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchInto().toList()
    Assertions.assertThat(r).isNotNull
    Assertions.assertThat(r).hasSize(1)
    Assertions.assertThat(r[0].id).isNotNull
  }

  @Test
  fun testReturningId() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id)
    val r = base.fetchOne()
    Assertions.assertThat(r).isNotNull
    Assertions.assertThat(r!!.containsKey("id")).isTrue
  }

  @Test
  fun testReturningId_noResultsBack() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("Michele", 1).onDuplicateKeyIgnore().returning(id)
    val r = base.fetchOneInto()
    Assertions.assertThat(r).isNull()
  }

  @Test
  fun testReturningMultipleFields() {
    val base = InsertQueryBuilder(table, txManager, "H2")
    base.columns(name, fieldName).values("name", 1).returning(id, name, fieldName)
    val r = base.fetchOne()
    Assertions.assertThat(r).isNotNull
    Assertions.assertThat(r!!.containsKey("id")).isTrue
    Assertions.assertThat(r.containsKey("name")).isTrue
    Assertions.assertThat(r.containsKey("field_name")).isTrue
  }

  @Test
  fun shouldCheckImproperUsageOfColumnsMethod() {
    val qb = h2Qb()
    qb.set(name, "")
    Assertions.assertThatThrownBy {
      qb.columns(name, id)
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Columns are specified automatically when you use set(Field, value)")
  }

  @Test
  fun shouldThrowWhenNoColumnsHaveBeenSpecified() {
    val qb = h2Qb()
    Assertions.assertThatThrownBy {
      qb.toSQL()
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("You need to defined at least one column to insert")
  }

  @Test
  fun shouldThrowWhenNoValuesHaveBeenSpecified() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    Assertions.assertThatThrownBy {
      qb.toSQL()
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Missing values to insert")
  }

  @Test
  fun shouldThrowWhenCallinValuesAfterSelect() {
    val qb = h2Qb()
    qb.select(qb())
    Assertions.assertThatThrownBy {
      qb.values(1, 2)
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot specify values when using select insert")
  }

  @Test
  fun shouldThrowWhenCallingSelectAfterValues() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    qb.values(1, 2)
    Assertions.assertThatThrownBy {
      qb.select(qb())
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot use select after you've used set method")
  }

  @Test
  fun shouldThrowWhenCallingSelectAfterSet() {
    val qb = h2Qb()
    qb.set(name, "")
    Assertions.assertThatThrownBy {
      qb.select(qb())
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot use select after you've used values method")
  }

  @Test
  fun shouldThrowWhenCallingSetAfterSelect() {
    val qb = h2Qb()
    qb.select(qb())
    Assertions.assertThatThrownBy {
      qb.set(name, "")
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot use set method when using select insert")
  }

  @Test
  fun shouldThrowWhenCallingSetAfterValues() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    qb.values("name", 2)
    Assertions.assertThatThrownBy {
      qb.set(name, "")
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot use set method when using multiple records")
  }

  @Test
  fun shouldThrowWhenCallingSetMultipleTimesWithTheSameColumn() {
    val qb = h2Qb()
    qb.set(name, "1")
    Assertions.assertThatThrownBy {
      qb.set(name, "2")
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot set same field multiple times")
  }

  @Test
  fun shouldThrowWhenCallingSetAfterColumns() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    Assertions.assertThatThrownBy {
      qb.set(name, "2")
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot use set after calling columns")
  }

  @Test
  fun shouldThrowWhenCallingValuesWithInvalidNumberOfColumns() {
    val qb = h2Qb()
    qb.columns(name, fieldName)
    Assertions.assertThatThrownBy {
      qb.values("a")
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Specified values don't match the number of columns")
  }

  @Test
  fun shouldThrowWhenCallingValuesAfterCallingSet() {
    val qb = h2Qb()
    qb.set(name, "")
    Assertions.assertThatThrownBy {
      qb.values(1, 2)
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot mix and match set(Field, value) with values")
  }

  companion object {
    fun h2Qb() = InsertQueryBuilder(table, mockk(), "H2")
    fun pgQb() = InsertQueryBuilder(table, mockk(), "Postgresql")
    fun qb(vararg selectFieldOrAsterisk: Expression<*>) = SelectQueryBuilder(mockk(), TestEntity::class.java, *selectFieldOrAsterisk).from(testRelationTable)

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
    private val table = TestEntity::class.java.table()

    @JvmStatic
    private val testRelationTable = TestRelation::class.java.table()

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