package com.experive.buddy.mapping

import com.experive.buddy.TestEntity
import com.experive.buddy.impl.Introspector
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import kotlin.reflect.KClass

@Table(name = "my_table")
data class AnnotatedTable(val id: Int)

@Table(name = "myTable")
data class AnnotatedTable2(@Id @GeneratedValue val id: Int, @Column(name = "display_name") val name: String)

internal class IntrospectorTest {

  @ParameterizedTest
  @MethodSource("tableNames")
  internal fun tableNamesAreCorrectlyExtracted(entity: KClass<*>, expectedName: String) {
    val table = Introspector.analyze(entity)
    assertThat(table.name).isEqualTo(expectedName)
  }

  @Test
  internal fun columnsAreIdentified() {
    val table = Introspector.analyze(TestEntity::class)
    assertThat(table.columns).hasSize(4)
    assertThat(table.columns["id"]).isNotNull()
    assertThat(table.columns["id"]!!.id).isTrue()
  }

  @Test
  internal fun columnsMetadataisReadFromAnnotation() {
    val table = Introspector.analyze(AnnotatedTable2::class)
    assertThat(table.columns).hasSize(2)
    val idColumn = table.columns["id"]
    assertThat(idColumn).isNotNull()
    assertThat(idColumn!!.id).isTrue()
    assertThat(idColumn.name).isEqualTo("id")
    assertThat(idColumn.generated).isEqualTo(true)
    assertThat(idColumn.insertable).isEqualTo(false)
    assertThat(idColumn.updatable).isEqualTo(false)

    val nameColumn = table.columns["name"]
    assertThat(nameColumn).isNotNull()
    assertThat(nameColumn!!.id).isFalse()
    assertThat(nameColumn.name).isEqualTo("display_name")
    assertThat(nameColumn.generated).isEqualTo(false)
    assertThat(nameColumn.insertable).isEqualTo(true)
    assertThat(nameColumn.updatable).isEqualTo(true)

  }


  companion object {
    @JvmStatic
    fun tableNames(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(TestEntity::class, "test_entity"),
        Arguments.of(AnnotatedTable::class, "my_table"),
        Arguments.of(AnnotatedTable2::class, "myTable"),
      )
    }
  }
}