package com.experive.buddy.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class AnyMapTest {
  val underTest = AnyMap(Source(1, "hello"))

  @Test
  internal fun getNonExistingFieldReturnsNull() {
    assertThat(underTest["something"]).isNull()
  }

  @Test
  internal fun getExistingFieldReturnsValue() {
    assertThat(underTest["id"]).isEqualTo(1)
    assertThat(underTest["name"]).isEqualTo("hello")
  }

  @Test
  internal fun getEntries() {
    val map = underTest.entries.associate { it.key to it.value }
    assertThat(map).isEqualTo(mapOf("id" to 1L, "name" to "hello"))
  }

}