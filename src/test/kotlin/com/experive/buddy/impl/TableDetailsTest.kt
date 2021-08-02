package com.experive.buddy.impl

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FuckedUpEntity(val value: Int) {
  var id: Int? = null
}

internal class TableDetailsTest {

  @Test
  @DisplayName("Should throw exception when provided with an invalid entity")
  fun newInstance_shouldThrow() {
    val tableDetails = Introspector.analyze(FuckedUpEntity::class)
    val exception = assertThrows<IllegalStateException> { tableDetails.newInstance(mapOf("value" to 5, "id" to 3)) }
    assertThat(exception).hasMessageThat().isEqualTo("No constructor for entity: class com.experive.buddy.impl.FuckedUpEntity")
  }
}