package com.experive.buddy

import com.beust.klaxon.JsonObject
import com.experive.buddy.support.BuddyPostgresExtension
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BuddyPostgresExtension::class)
internal class PgsqlCoreDatabaseTest : DatabaseCoreQueries() {
  @BeforeEach
  internal fun setUp() {
    underTest = Database.using(txManager)

    underTest.execute("create table if not exists test_entity (id serial primary key, name text, field_name int, boolean_field boolean)")
    underTest.execute("create table if not exists test_relation (id serial primary key, test_id int, active boolean)")
    underTest.execute("create table if not exists test_json (id serial primary key, map jsonb, relation jsonb)")
    underTest.persistMany(
      arrayListOf(
        TestEntity(null, "Miguél", 1, true),
        TestEntity(null, "MIGUÉL", 3),
        TestEntity(null, "Michael", 2, true),
        TestEntity(null, "Michele", 3)
      )
    ).execute()

    michaelId = underTest.persist(TestEntity(null, "Michel", 4, true))
      .returning()
      .fetchSingleInto(Int::class.java)

    mikuId = underTest.persist(TestEntity(null, "Miku", 27))
      .returning()
      .fetchSingleInto(Int::class.java)

    mendelId = underTest.persist(TestEntity(null, "Mendel", 27, true))
      .returning()
      .fetchSingleInto(Int::class.java)

    underTest.persist(TestEntity(null, null, 27)).execute()

    underTest.insertInto(testRelationTable)
      .set(trTestId, michaelId)
      .set(trActive, true)
      .execute()

    underTest.insertInto(testRelationTable)
      .set(trTestId, mikuId)
      .set(trActive, true)
      .execute()

    underTest.insertInto(testRelationTable)
      .set(trTestId, mendelId)
      .set(trActive, false)
      .execute()

  }

  @Test
  internal fun testOrderByDesc() {
    val record = underTest
      .selectFrom(table)
      .orderBy(name, Direction.DESC)
      .fetch()

    val results = record.map { it.into(TestEntity::class.java) }.toList()
    assertThat(results).hasSize(8)
    assertThat(results.map { it.name }).isEqualTo(
      arrayListOf(
        "Miku", "MIGUÉL", "Miguél", "Michele", "Michel", "Michael", "Mendel", null
      )
    )
  }

  @Test
  internal fun testJsonQuery() {
    underTest.persistMany(
      listOf(
        TestJson(null, JsonObject(mapOf("a" to "1")), null),
        TestJson(null, JsonObject(mapOf("a" to "2")), null),
        TestJson(null, JsonObject(mapOf("a" to "3")), null),
        TestJson(null, JsonObject(mapOf("a" to "3")), null),
        TestJson(null, JsonObject(mapOf("a" to "2")), null),
        TestJson(null, JsonObject(mapOf("a" to "5")), null),
      )
    ).execute()
    val jsonProp = jsonTable.column<JsonObject>("map")!!
    val res = underTest.selectFrom(jsonTable)
      .where(jsonProp.get<String>("a").eq("3"))
      .fetchInto().toList()

    assertThat(res.map { it.map!!.string("a") }).isEqualTo(arrayListOf("3", "3"))
  }
}

