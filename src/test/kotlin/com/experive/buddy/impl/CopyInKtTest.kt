package com.experive.buddy.impl

import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

internal class CopyInKtTest {
  @Test
  internal fun unquotedString() {
    val sb = StringBuilder()
    quote(sb, "hello")
    Truth.assertThat(sb.toString()).isEqualTo("\"hello\"")
  }

  @Test
  internal fun escapeProperlyOnceString() {
    val sb = StringBuilder()
    quote(sb, "hello\" world")
    Truth.assertThat(sb.toString()).isEqualTo("\"hello\"\" world\"")
  }

  @Test
  internal fun escapeProperlyMultipleTimes() {
    val sb = StringBuilder()
    quote(sb, "{\"name:en\": \"some\" }")
    Truth.assertThat(sb.toString()).isEqualTo("\"{\"\"name:en\"\": \"\"some\"\" }\"")
  }


}