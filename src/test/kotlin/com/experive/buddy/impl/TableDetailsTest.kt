package com.experive.buddy.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FuckedUpEntity(val value: Int) {
  var id: Int? = null
}

internal class TableDetailsTest {

  @Test
  @DisplayName("Should throw exception when provided with an invalid entity")
  fun newInstance_shouldThrow() {
    val tableDetails = Introspector.analyze(FuckedUpEntity::class.java)
    Assertions.assertThatThrownBy {
      tableDetails.newInstance(mapOf("value" to 5, "id" to 3))
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessage("No constructor for entity: class com.experive.buddy.impl.FuckedUpEntity")
  }
}