package com.experive.buddy

import com.experive.benchmark.BenchmarkRunner
import com.experive.buddy.mapper.json
import com.experive.buddy.support.BuddyPostgresExtension
import com.fasterxml.jackson.databind.JsonNode
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux

@ExtendWith(BuddyPostgresExtension::class)
internal class PgsqlCoreDatabaseTest : DatabaseCoreQueries() {
    @BeforeEach
    internal fun setUp() {
        underTest = Database.using(txManager)

        underTest.execute(
            "create table if not exists test_entity " +
                "(id serial primary key, name text, field_name int, boolean_field boolean)"
        )
        underTest.execute(
            "create table if not exists test_relation " +
                "(id serial primary key, test_id int, active boolean)"
        )
        underTest.execute(
            "create table if not exists test_json " +
                "(id serial primary key, map jsonb, relation jsonb)"
        )
        underTest.execute(
            "create table if not exists date_entity " +
                "(id serial primary key, local_time_field time, local_date_field date, " +
                "local_date_time_field timestamp, java_date_field timestamp, java_timestamp_field timestamp)"
        )
        underTest.execute(
            "create table if not exists geo_sample " +
                "(osm_id bigint, osm_type text, class text, type text, name jsonb, address jsonb)"
        )
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
            .fetchSingleInto(Int::class)

        mikuId = underTest.persist(TestEntity(null, "Miku", 27))
            .returning()
            .fetchSingleInto(Int::class)

        mendelId = underTest.persist(TestEntity(null, "Mendel", 27, true))
            .returning()
            .fetchSingleInto(Int::class)

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

        val results = record.map { it.into(TestEntity::class)!! }.toList()
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
                TestJson(null, json { put("a", "1") }, null),
                TestJson(null, json { put("a", "2") }, null),
                TestJson(null, json { put("a", "3") }, null),
                TestJson(null, json { put("a", "3") }, null),
                TestJson(null, json { put("a", "2") }, null),
                TestJson(null, json { put("a", "5") }, null),
            )
        ).execute()
        val jsonProp: Expression<JsonNode> = jsonTable.column("map")!!

        val res = underTest.selectFrom(jsonTable)
            .where(jsonProp.get<String>("a").eq("3"))
            .fetchInto().toList()

        assertThat(res.map { it.map!!.get("a").asText() }).isEqualTo(arrayListOf("3", "3"))
    }

    @Test
    internal fun testCopyIn() {
        val sampleData = (0 until 1000).map {
            GeoSample(
                null,
                it.toLong(),
                "some",
                "class",
                "type",
                "{\"name:en\": \"some\" }",
                "{ \"addr:name\": \"lol\" }"
            )
        }
        underTest.copyIn(GeoSample::class.table(), Flux.fromIterable(sampleData))
    }

    @Test
    @Tag("benchmark")
    internal fun benchmarkCopy() {
        val sampleData: List<GeoSample> = (0 until 200_000).map {
            GeoSample(
                null,
                it.toLong(),
                "some",
                "class",
                "type",
                "{\"name:en\": \"some\" }",
                "{ \"addr:name\": \"lol\" }"
            )
        }
        BenchmarkRunner()
            .add(this::copy, sampleData)
            .runAll()
    }

    fun copy(sampleData: List<GeoSample>) {
        underTest.copyIn(GeoSample::class.table(), Flux.fromIterable(sampleData))
    }
}
