package com.experive.buddy.mapping

import com.experive.buddy.TestEntity
import com.experive.buddy.annotations.Column
import com.experive.buddy.annotations.GeneratedValue
import com.experive.buddy.annotations.Id
import com.experive.buddy.annotations.Table
import com.experive.buddy.impl.Introspector
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@Table("my_table")
data class AnnotatedTable(val id: Int)

@Table("myTable")
data class AnnotatedTable2(@Id @GeneratedValue val id: Int, @Column("display_name") val name: String)

internal class IntrospectorTest {

  @ParameterizedTest
  @MethodSource("tableNames")
  internal fun tableNamesAreCorrectlyExtracted(entity: Class<*>, expectedName: String) {
    val table = Introspector.analyze(entity)
    Assertions.assertThat(table.name).isEqualTo(expectedName)
  }

  @Test
  internal fun columnsAreIdentified() {
    val table = Introspector.analyze(TestEntity::class.java)
    Assertions.assertThat(table.columns).hasSize(4)
    Assertions.assertThat(table.columns["id"]).isNotNull
    Assertions.assertThat(table.columns["id"]!!.id).isTrue
  }

  @Test
  internal fun columnsMetadataisReadFromAnnotation() {
    val table = Introspector.analyze(AnnotatedTable2::class.java)
    Assertions.assertThat(table.columns).hasSize(2)
    val idColumn = table.columns["id"]
    Assertions.assertThat(idColumn).isNotNull
    Assertions.assertThat(idColumn!!.id).isTrue
    Assertions.assertThat(idColumn.name).isEqualTo("id")
    Assertions.assertThat(idColumn.generated).isEqualTo(true)
    Assertions.assertThat(idColumn.insertable).isEqualTo(false)
    Assertions.assertThat(idColumn.updatable).isEqualTo(false)

    val nameColumn = table.columns["name"]
    Assertions.assertThat(nameColumn).isNotNull
    Assertions.assertThat(nameColumn!!.id).isFalse
    Assertions.assertThat(nameColumn.name).isEqualTo("display_name")
    Assertions.assertThat(nameColumn.generated).isEqualTo(false)
    Assertions.assertThat(nameColumn.insertable).isEqualTo(true)
    Assertions.assertThat(nameColumn.updatable).isEqualTo(true)

  }


  companion object {
    @JvmStatic
    fun tableNames(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(TestEntity::class.java, "test_entity"),
        Arguments.of(AnnotatedTable::class.java, "my_table"),
        Arguments.of(AnnotatedTable2::class.java, "myTable"),
      )
    }
  }
}