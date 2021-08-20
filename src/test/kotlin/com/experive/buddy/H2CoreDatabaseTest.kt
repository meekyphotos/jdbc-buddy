package com.experive.buddy

import com.experive.buddy.support.BuddyH2Extension
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BuddyH2Extension::class)
internal class H2CoreDatabaseTest : DatabaseCoreQueries() {

    @BeforeEach
    internal fun setUp() {
        underTest = Database.using(txManager)

        underTest.execute(
            "create table if not exists test_entity " +
                "(id int primary key auto_increment, name varchar, field_name int, boolean_field boolean)"
        )
        underTest.execute(
            "create table if not exists test_relation " +
                "(id int primary key auto_increment, test_id int, active boolean)"
        )
        underTest.execute(
            "create table if not exists test_json " +
                "(id int primary key auto_increment, map json, relation json)"
        )
        underTest.execute(
            "create table if not exists date_entity " +
                "(id int primary key auto_increment, local_time_field time, local_date_field date," +
                " local_date_time_field timestamp, java_date_field timestamp, java_timestamp_field timestamp)"
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
                "Miku", "Miguél", "Michele", "Michel", "Michael", "Mendel", "MIGUÉL", null
            )
        )
    }
}
