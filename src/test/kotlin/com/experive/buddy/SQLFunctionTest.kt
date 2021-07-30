package com.experive.buddy

import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

internal class SQLFunctionTest {
  @Test
  internal fun acceptsNoParameters() {
    val underTest = SQLFunction<String, String>("fnName")
    Truth.assertThat(underTest.toSqlFragment()).isEqualTo("fnName()")
  }

  @Test
  internal fun acceptsOneArgument() {
    val underTest = SQLFunction<String, String>("fnName", "name".asExpression())
    Truth.assertThat(underTest.toSqlFragment()).isEqualTo("fnName(?)")
    Truth.assertThat(underTest.collectValues()).isEqualTo(arrayListOf("name"))
  }

  @Test
  internal fun acceptsManyArgument() {
    val underTest = SQLFunction<String, String>("fnName", "arg0".asExpression(), "arg1".asExpression())
    Truth.assertThat(underTest.toSqlFragment()).isEqualTo("fnName(?, ?)")
    Truth.assertThat(underTest.collectValues()).isEqualTo(arrayListOf("arg0", "arg1"))
  }

}