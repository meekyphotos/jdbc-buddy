package com.experive.buddy.dialect

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test

internal class DefaultDialectTest {
  @Test
  internal fun supportReturningShouldReturnFalse() {
    val underTest = Dialect.of("")
    assertThat(underTest.supportReturning()).isFalse()
  }

  @Test
  internal fun emitPlaceholderIsAlwaysQuestionMark() {
    val underTest = Dialect.of("")
    assertThat(underTest.emitPlaceholder(JSONObject::class.java)).isEqualTo("?")
    assertThat(underTest.emitPlaceholder(String::class.java)).isEqualTo("?")
    assertThat(underTest.emitPlaceholder(Any::class.java)).isEqualTo("?")
  }

  @Test
  internal fun emitOnConflictDoNothing() {
    val underTest = Dialect.of("")
    val text = StringBuilder()
    underTest.emitOnConflictDoNothing(text)
    assertThat(text.toString()).isEqualTo(" ON CONFLICT DO NOTHING")
  }

  @Test
  internal fun emitOnDuplicateKeyIgnore() {
    val underTest = Dialect.of("")
    val text = StringBuilder()
    underTest.emitOnDuplicateKeyIgnore(text)
    assertThat(text.toString()).isEqualTo(" ON CONFLICT DO NOTHING")
  }

  @Test
  internal fun emitReturning() {
    val underTest = Dialect.of("")
    val text = StringBuilder()
    underTest.emitReturning(text, emptyList())
    assertThat(text.toString()).isEqualTo("")
  }
}