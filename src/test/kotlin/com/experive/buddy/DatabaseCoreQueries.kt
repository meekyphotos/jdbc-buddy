package com.experive.buddy

import com.experive.buddy.exceptions.NoDataFoundException
import com.experive.buddy.exceptions.TooManyRowsException
import com.experive.buddy.mapper.json
import com.experive.buddy.mapper.jsonArray
import com.experive.buddy.predicates.Predicate
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset
import java.util.Date
import java.util.stream.Stream

// This class is abstract so that it is not considered as standalone testing class
@SuppressWarnings("UnnecessaryAbstractClass")
abstract class DatabaseCoreQueries {
    var michaelId: Int = 0
    var mikuId: Int = 0
    var mendelId: Int = 0

    lateinit var txManager: JdbcTemplate
    lateinit var underTest: Database

    @ParameterizedTest
    @MethodSource("queries")
    @DisplayName("Standard entities: various queries")
    internal fun shouldReturnExpectedResults(predicate: Predicate?, names: List<String>) {
        val builder = underTest
            .selectFrom(table)
        if (predicate != null) {
            builder.where(predicate)
        }
        val record = builder.fetchInto()
        val results = record.toList()
        assertThat(results).hasSize(names.size)
        assertThat(results.map { it.name }).isEqualTo(names)
    }

    @ParameterizedTest
    @MethodSource("joinQueries")
    @DisplayName("Join entities: various queries")
    internal fun shouldReturnExpectedResultsUsingJoin(predicate: Predicate?, names: List<String>) {
        val builder = underTest
            .selectForEntity(table.enclosingType)
            .from(table).join(testRelationTable, trTestId)
        if (predicate != null) {
            builder.where(predicate and trActive.isTrue())
        } else {
            builder.where(trActive.isTrue())
        }
        val record = builder.fetchInto()
        val results = record.toList()
        assertThat(results).hasSize(names.size)
        assertThat(results.map { it.name }).isEqualTo(names)
    }

    @ParameterizedTest
    @MethodSource("leftJoinQueries")
    @DisplayName("Left join entities: various queries")
    internal fun shouldReturnExpectedResultsUsingLeftJoin(predicate: Predicate?, names: List<String>) {
        val builder = underTest
            .select()
            .from(table).leftJoin(testRelationTable, trTestId)
        if (predicate != null) {
            builder.where(predicate, (trActive.isTrue() or trActive.isNull()))
        } else {
            builder.where(trActive.isTrue() or trActive.isNull())
        }
        val record = builder.fetchInto(TestEntity::class)
        val results = record.toList()
        assertThat(results).hasSize(names.size)
        assertThat(results.map { it.name }).containsExactly(*names.toTypedArray())
    }

    @ParameterizedTest
    @MethodSource("rightJoinQueries")
    @DisplayName("Right join entities: various queries")
    internal fun shouldReturnExpectedResultsUsingRightJoin(predicate: Predicate?, names: List<String>) {
        val builder = underTest
            .selectForEntity(table.enclosingType)
            .from(table).rightJoin(testRelationTable, trTestId)
        if (predicate != null) {
            builder.where(predicate, trActive.isTrue())
        } else {
            builder.where(trActive.isTrue())
        }
        val record = builder.fetchInto()
        val results = record.toList()
        assertThat(results).hasSize(names.size)
        assertThat(results.map { it.name }).isEqualTo(names)
    }

    @ParameterizedTest
    @MethodSource("queries")
    @DisplayName("Immutable entities: various queries")
    internal fun shouldMapImmutableEntities(predicate: Predicate?, names: List<String>) {
        val builder = underTest
            .selectFrom(table)
        if (predicate != null) {
            builder.where(predicate)
        }
        val record = builder.fetchInto(ImmutableTestEntity::class)
        val results = record.toList()
        assertThat(results).hasSize(names.size)
        assertThat(results.map { it.name }).isEqualTo(names)
    }

    @ParameterizedTest
    @MethodSource("queries")
    @DisplayName("Entities with empty constructor: various queries")
    internal fun shouldMapEntitiesWithEmptyConstructors(predicate: Predicate?, names: List<String>) {
        val builder = underTest
            .selectFrom(table)
        if (predicate != null) {
            builder.where(predicate)
        }
        val record = builder.fetchInto(ForcedEmptyConstructorTestEntity::class)

        val results = record.toList()
        assertThat(results).hasSize(names.size)
        assertThat(results.map { it.name }).isEqualTo(names)
    }

    @ParameterizedTest
    @MethodSource("countQueries")
    @DisplayName("Count: various queries")
    internal fun shouldReturnExpectedCount(predicate: Predicate?, count: Int) {
        val builder = underTest
            .select(count())
            .from(table)
        if (predicate != null) {
            builder.where(predicate)
        }
        val record = builder.fetchOneInto(Long::class)

        assertThat(record).isNotNull()
        assertThat(record).isEqualTo(count.toLong())
    }

    @Test
    @DisplayName("Find one: should return null when id does not exist")
    internal fun shouldReturnEmptyOptionalWhenIdDoesNotExist() {
        val result = underTest.byId(6545132132L, table).fetchOneInto()
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("Find one: should return null when id does not exist even if mapped")
    internal fun shouldReturnNullWhenIdDoesNotExistAndMapped() {
        val result = underTest.byId(6545132132L, table).fetchOneInto(TestEntity::class)
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("Find one: should return the entity when exists")
    internal fun fetchone_shouldReturnSomethingWhenIdExists() {
        val result = underTest.byId(mikuId, table).fetchOneInto()
        assertThat(result).isNotNull()
    }

    @Test
    @DisplayName("Select supports forEach")
    internal fun shouldAllowToUseForEach() {
        val result = underTest.selectFrom(table).where(fieldName eq 27).map { it.name }
        assertThat(result).isEqualTo(listOf("Miku", "Mendel", null))
    }

    @Test
    @DisplayName("Find one: should throw exception when id does not exist")
    internal fun shouldThrowWhenIdDoesNotExist() {
        val e = assertThrows<NoDataFoundException> { underTest.byId(6545132132L, table).fetchSingleInto() }
        assertThat(e).isInstanceOf(NoDataFoundException::class.java)
    }

    @Test
    @DisplayName("fetchSingleInto: should throw exception when more than one element is returned")
    internal fun shouldThrowWhenMultipleElements() {
        val e = assertThrows<TooManyRowsException> {
            underTest.selectFrom(table).where(fieldName.eq(27)).fetchSingleInto()
        }
        assertThat(e).isInstanceOf(TooManyRowsException::class.java)
    }

    @Test
    @DisplayName("Find one: should return the entity when id exists")
    internal fun shouldReturnSomethingWhenIdExists() {
        val expected = underTest.selectFrom(table).limit(1).fetchSingleInto()
        val result = underTest.byId(expected.id, table).fetchSingleInto()
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("Persist: should increase count by 1")
    internal fun persistShouldIncreaseCountBy1() {
        assertThat(underTest.selectCount(table).fetchSingleInto(Long::class)).isEqualTo(8L)
        underTest.persist(TestEntity(name = "Hello")).execute()
        assertThat(underTest.selectCount(table).fetchSingleInto(Long::class)).isEqualTo(9L)
        assertThat(
            underTest.selectCount(table).where(
                name.eq("Hello")
            ).fetchSingleInto(Long::class)
        ).isEqualTo(1L)
    }

    @Test
    @DisplayName("PersistMany: should increase count by 4")
    internal fun persistShouldIncreaseCountBy2() {
        assertThat(underTest.selectCount(table).fetchOneInto(Long::class)).isEqualTo(8L)
        underTest.persistMany(
            arrayListOf(
                TestEntity(name = "Hello"),
                TestEntity(name = "Hello"),
                TestEntity(name = "Hello"),
                TestEntity(name = "Hello")
            )
        ).execute()
        assertThat(underTest.selectCount(table).fetchSingleInto(Long::class)).isEqualTo(12L)
        assertThat(
            underTest.selectCount(table).where(
                name.eq("Hello")
            ).fetchSingleInto(Long::class)
        ).isEqualTo(4L)
    }

    @Test
    @DisplayName("Select: should return three elements when using OR")
    internal fun shouldReturnThreeElementsWhenCominingUsingOr() {
        val record = underTest
            .selectFrom(table)
            .where(name.`in`("Miguél", "Michele") or name.eq("Miku"))
            .fetch()

        val results = record.map { it.into(TestEntity::class)!! }.toList()
        assertThat(results).hasSize(3)
        assertThat(results.map { it.name!! }).containsExactly("Miguél", "Michele", "Miku")
    }

    @Test
    @DisplayName("Select: should return one element when using +")
    internal fun shouldReturnOneElementWhenCombiningUsingPlus() {
        val record = underTest
            .selectFrom(table)
            .where(name.`in`("Miguél", "Michele") + name.eq("Michele"))
            .fetch()

        val results = record.map { it.into(TestEntity::class)!! }.toList()
        assertThat(results).hasSize(1)
        assertThat(results.map { it.name!! }).contains("Michele")
    }

    @Test
    @DisplayName("Select: should return one element when using AND")
    internal fun shouldReturnOneElementWhenCombiningInAnd() {
        val record = underTest
            .selectFrom(table)
            .where(name.`in`("Miguél", "Michele") and name.eq("Michele"))
            .fetch()

        val results = record.map { it.into(TestEntity::class)!! }.toList()
        assertThat(results).hasSize(1)
        assertThat(results.map { it.name!! }).contains("Michele")
    }

    @Test
    @DisplayName("Select: should return a boolean when using predicate in selection")
    internal fun shouldReturnBooleanWhenUsingPredicateAsSelection() {
        val name = table.column<String>("name")!!
        val record = underTest
            .select(name.eq("Michele"))
            .from(table)
            .where(name.`in`("Miguél", "Michele"))
            .fetch()

        val results = record.map { it.into(Boolean::class) }.toList()
        assertThat(results).hasSize(2)
        assertThat(results).isEqualTo(arrayListOf(false, true))
    }

    @Test
    @DisplayName("Testing record API")
    internal fun shouldAllowToMapResultsIntoEntities() {
        val record = underTest
            .selectFrom(table)
            .where(name.`in`("Miguél", "Michele"))
            .fetch()
            .map { it.into(TestEntity::class)!! }
            .map { it.name }

        val results = HashSet<String>()
        while (record.hasNext()) {
            val element = record.next()
            results.add(element!!)
        }
        assertThat(results).hasSize(2)
        assertThat(results).containsExactly("Miguél", "Michele")
    }

    @Test
    @DisplayName("Update: should update the entity")
    internal fun shouldUpdateTheEntity() {
        val affectedRows = underTest
            .update(table)
            .set(name, "Michelee")
            .where(fieldName.eq(27))
            .execute()
        assertThat(affectedRows).isEqualTo(3)
        val countAfter = underTest.selectCount(table)
            .where(name.eq("Michelee"))
            .fetchOneInto(Long::class)
        assertThat(countAfter).isEqualTo(3)
    }

    @Test
    @DisplayName("Update: should update the entity")
    internal fun shouldUpdateTheEntityUsingUpdateRef() {
        val affectedRow = underTest.byId(mikuId, table).fetchSingleInto()
        affectedRow.name = "Michelee"
        underTest.update(affectedRow).execute()

        val countAfter = underTest.selectCount(table)
            .where(name.eq("Michelee"))
            .fetchOneInto(Long::class)
        assertThat(countAfter).isEqualTo(1)
    }

    @Test
    @DisplayName("Delete: should update the entity")
    internal fun shouldDeleteTheEntityUsingDeleteRef() {
        val affectedRow = underTest.byId(mikuId, table).fetchSingleInto()
        underTest.delete(affectedRow).execute()

        val countAfter = underTest.selectCount(table)
            .where(id.eq(mikuId))
            .fetchOneInto(Long::class)
        assertThat(countAfter).isEqualTo(0)
    }

    @Test
    @DisplayName("DeleteOne: should delete the provided Id")
    internal fun shouldDeleteTheProvidedId() {
        val count = underTest.selectCount(table).fetchOneInto(Long::class)
        assertThat(count).isEqualTo(8)
        val idToDelete = underTest
            .selectFrom(table)
            .where(table.column<String>("name")!!.eq("Michele"))
            .fetchSingle()
            .into(table.enclosingType)!!
            .id

        underTest.deleteById(idToDelete, table).execute()

        val countAfter = underTest
            .selectCount(table)
            .where(id.eq(idToDelete))
            .fetchSingleInto(Long::class)

        assertThat(countAfter).isEqualTo(0)
    }

    @Test
    @DisplayName("DeleteOne: does nothing when the Id does not exists")
    internal fun shouldNotDeleteTheProvidedId() {
        val count = underTest.selectCount(table).fetchOneInto(Long::class)
        assertThat(count).isEqualTo(8)
        underTest.deleteById(84651320L, table)

        val countAfter = underTest.selectCount(table).fetchOneInto(Long::class)
        assertThat(countAfter).isEqualTo(8)
    }

    @Test
    @DisplayName("Select: should return results sorted by fieldName ASC")
    internal fun testOrderByAsc() {
        val record = underTest
            .selectFrom(table)
            .orderBy(fieldName, Direction.ASC)
            .fetch()

        val results = record.toList()
        assertThat(results).hasSize(8)
        assertThat(results[0].containsValue("Miguél"))
        assertThat(results.map { it["name"] }).isEqualTo(
            arrayListOf(
                "Miguél", "Michael", "MIGUÉL", "Michele", "Michel", "Miku", "Mendel", null
            )
        )
    }

    @Test
    @DisplayName("Select: requesting first page should return first 3 elements")
    internal fun testLimit() {
        val table = TestEntity::class.table()
        val record = underTest
            .selectFrom(table)
            .limit(3)
            .fetch()

        val results = record.map { it.into(TestEntity::class)!! }.toList()
        assertThat(results).hasSize(3)
        assertThat(results.map { it.name!! }).isEqualTo(
            arrayListOf(
                "Miguél", "MIGUÉL", "Michael"
            )
        )
    }

    @Test
    @DisplayName("Select: requesting second page should return other 3 elements")
    internal fun testSecondPage() {
        val record = underTest
            .selectFrom(table)
            .limit(3)
            .offset(3)
            .fetch()

        val results = record.map { it.into(TestEntity::class)!! }.toList()
        assertThat(results).hasSize(3)
        assertThat(results.map { it.name!! }).isEqualTo(
            arrayListOf(
                "Michele", "Michel", "Miku"
            )
        )
    }

    @Test
    @DisplayName("Delete: should delete all the elements matching the query (2)")
    internal fun testDelete() {
        underTest.delete(table).where(fieldName.eq(27)).execute()
        val count = underTest.selectCount(table).fetchOneInto(Long::class)
        assertThat(count).isEqualTo(5)
    }

    @Test
    @DisplayName("Select: test aliasing")
    internal fun testAliasing() {
        val target = AliasedTestEntity::class
        val result = underTest.select(name.`as`("user_name"), fieldName.`as`("login_count"))
            .from(table)
            .where(fieldName.eq(27) and name.isNotNull())
            .fetchInto(target)

        assertThat(result.toList()).isEqualTo(
            arrayListOf(
                AliasedTestEntity("Miku", 27),
                AliasedTestEntity("Mendel", 27)
            )
        )
    }

    @Test
    internal fun testJsonSupport_usingMap() {

        val column = jsonTable.column(TestJson::map)
        val idColumn = jsonTable.column(TestJson::id)
        val id = underTest.insertInto(jsonTable)
            .set(column, json { put("a", "b") })
            .returning(idColumn)
            .fetchSingleInto(Int::class)

        val json = underTest
            .select()
            .from(jsonTable)
            .where(idColumn eq id)
            .fetchSingleInto(jsonTable.enclosingType)

        assertThat(json.map).isEqualTo(json { put("a", "b") })
    }

    @Test
    internal fun standardFetch() {
        val count = underTest.fetch("select count(*) from test_entity")
            .map { it.into(Long::class) }
            .toList()
            .firstOrNull()
        assertThat(count).isEqualTo(8)
    }

    @Test
    internal fun testJsonSupport_usingArrays() {

        val column = jsonTable.column(TestJson::relation)
        val idColumn = jsonTable.column(TestJson::id)
        val id = underTest.insertInto(jsonTable)
            .set(column, jsonArray { add(1); add(2); add(3) })
            .returning(idColumn).fetchSingleInto(Int::class)

        val json = underTest
            .select()
            .from(jsonTable)
            .where(idColumn eq id)
            .fetchSingleInto(jsonTable.enclosingType)

        assertThat(json.relation).isEqualTo(jsonArray { (1..3).forEach { add(it) } })
    }

    @Test
    internal fun testDateSupport() {
        val time = LocalTime.of(12, 34, 56)
        val date = LocalDate.of(2021, Month.FEBRUARY, 14)
        val dateTime = LocalDateTime.of(date, time)
        val persistEntity = DateEntity(
            null,
            time,
            date,
            dateTime,
            Date.from(dateTime.toInstant(ZoneOffset.UTC)),
            Timestamp.valueOf(dateTime)
        )
        underTest.persist(persistEntity).execute()

        val dateEntity = underTest.selectFrom(dateTable).fetchSingleInto()
        assertThat(persistEntity.localTimeField).isEqualTo(dateEntity.localTimeField)
        assertThat(persistEntity.localDateField).isEqualTo(dateEntity.localDateField)
        assertThat(persistEntity.localDateTimeField).isEqualTo(dateEntity.localDateTimeField)
        assertThat(persistEntity.javaDateField).isEqualTo(dateEntity.javaDateField)
        assertThat(persistEntity.javaTimestampField).isEqualTo(dateEntity.javaTimestampField)
    }

    companion object {
        @JvmStatic
        protected val table = TestEntity::class.table()

        @JvmStatic
        protected val dateTable = DateEntity::class.table()

        @JvmStatic
        protected val jsonTable = TestJson::class.table()

        @JvmStatic
        protected val testRelationTable = TestRelation::class.table()

        @JvmStatic
        protected val trTestId = testRelationTable.column(TestRelation::testId)

        @JvmStatic
        protected val trActive = testRelationTable.column(TestRelation::active)

        @JvmStatic
        protected val name = table.column(TestEntity::name)

        @JvmStatic
        protected val fieldName = table.column(TestEntity::fieldName)

        @JvmStatic
        protected val id = table.column(TestEntity::id)

        @JvmStatic
        protected val booleanField = table.column(TestEntity::booleanField)

        @JvmStatic
        fun queries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(name.eq("Miguél"), arrayListOf("Miguél")),
                Arguments.of(
                    name.notEqual("Miguél"),
                    arrayListOf("MIGUÉL", "Michael", "Michele", "Michel", "Miku", "Mendel")
                ),
                Arguments.of(lower(name).eq("miguél"), arrayListOf("Miguél", "MIGUÉL")),
                Arguments.of(upper(name).eq("MIGUÉL"), arrayListOf("Miguél", "MIGUÉL")),
                Arguments.of(
                    name.`in`("Miguél", "Michele"),
                    arrayListOf("Miguél", "Michele")
                ),
                Arguments.of(
                    name.notIn("Miguél", "Michele"),
                    arrayListOf("MIGUÉL", "Michael", "Michel", "Miku", "Mendel")
                ),
                Arguments.of(name.like("Mig%"), arrayListOf("Miguél")),
                Arguments.of(name.like("%él"), arrayListOf("Miguél")),
                Arguments.of(name.like("%ik%"), arrayListOf("Miku")),
                Arguments.of(name.like("mic%"), arrayListOf<String>()),
                Arguments.of(name.ilike("mic%"), arrayListOf("Michael", "Michele", "Michel")),
                Arguments.of(name.ilike("%CH%"), arrayListOf("Michael", "Michele", "Michel")),
                Arguments.of(name.isNull(), arrayListOf(null)),
                Arguments.of(
                    name.isNotNull(),
                    arrayListOf("Miguél", "MIGUÉL", "Michael", "Michele", "Michel", "Miku", "Mendel")
                ),
                Arguments.of(booleanField.isTrue(), arrayListOf("Miguél", "Michael", "Michel", "Mendel")),
                Arguments.of(booleanField.isFalse(), arrayListOf("MIGUÉL", "Michele", "Miku", null)),
                Arguments.of(fieldName.between(2, 10), arrayListOf("MIGUÉL", "Michael", "Michele", "Michel")),
                Arguments.of(fieldName.notBetween(2, 10), arrayListOf("Miguél", "Miku", "Mendel", null)),
                Arguments.of(fieldName.lessThan(3), arrayListOf("Miguél", "Michael")),
                Arguments.of(fieldName.lessOrEqual(3), arrayListOf("Miguél", "MIGUÉL", "Michael", "Michele")),
                Arguments.of(fieldName.greaterThan(27), arrayListOf<String>()),
                Arguments.of(fieldName.greaterOrEqual(27), arrayListOf("Miku", "Mendel", null)),
            )
        }

        @JvmStatic
        fun joinQueries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(name.eq("Miguél"), arrayListOf<String>()),
                Arguments.of(name.notEqual("Miguél"), arrayListOf("Michel", "Miku")),
                Arguments.of(fieldName.greaterOrEqual(27), arrayListOf("Miku")),
            )
        }

        @JvmStatic
        fun leftJoinQueries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(name.eq("Miguél"), arrayListOf("Miguél")),
                Arguments.of(name.notEqual("Miguél"), arrayListOf("MIGUÉL", "Michael", "Michele", "Michel", "Miku")),
                Arguments.of(fieldName.greaterOrEqual(27), arrayListOf("Miku", null)),
            )
        }

        @JvmStatic
        fun rightJoinQueries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(name.like("%ik%"), arrayListOf("Miku")),
                Arguments.of(name.like("mic%"), arrayListOf<String>()),
                Arguments.of(fieldName.greaterThan(27), arrayListOf<String>()),
            )
        }

        @JvmStatic
        fun countQueries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null, 8),
                Arguments.of(name.eq("Miguél"), 1),
                Arguments.of(name.notEqual("Miguél"), 6),
                Arguments.of(lower(name).eq("miguél"), 2),
                Arguments.of(upper(name).eq("MIGUÉL"), 2),
                Arguments.of(name.`in`("Miguél", "Michele"), 2),
                Arguments.of(name.notIn("Miguél", "Michele"), 5),
                Arguments.of(name.like("Mig%"), 1),
                Arguments.of(name.like("%él"), 1),
                Arguments.of(name.like("%ik%"), 1),
                Arguments.of(name.like("mic%"), 0),
                Arguments.of(name.ilike("mic%"), 3),
                Arguments.of(name.ilike("%CH%"), 3),
                Arguments.of(name.isNull(), 1),
                Arguments.of(name.isNotNull(), 7),
                Arguments.of(booleanField.isTrue(), 4),
                Arguments.of(booleanField.isFalse(), 4),
                Arguments.of(fieldName.between(2, 10), 4),
                Arguments.of(fieldName.notBetween(2, 10), 4),
                Arguments.of(fieldName.lessThan(3), 2),
                Arguments.of(fieldName.lessOrEqual(3), 4),
                Arguments.of(fieldName.greaterThan(27), 0),
                Arguments.of(fieldName.greaterOrEqual(27), 3),
            )
        }
    }
}
