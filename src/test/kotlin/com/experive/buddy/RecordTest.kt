package com.experive.buddy

import com.google.common.truth.Truth
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class RecordTest {

  @ParameterizedTest
  @MethodSource("conversions")
  internal fun shouldConvert(map: Map<String, Any>, clazz: Class<*>, expectedValue: Any?) {
    val r = Record(map)
    Truth.assertThat(r.into(clazz)).isEqualTo(expectedValue)
  }


  companion object {
    @JvmStatic
    fun conversions(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(mapOf("id" to 1), Int::class.java, 1),
        Arguments.of(mapOf("id" to 1.0), Double::class.java, 1.0),
        Arguments.of(mapOf("id" to 1L), Long::class.java, 1L),
        Arguments.of(mapOf("id" to 1.0f), Float::class.java, 1.0f),
        Arguments.of(mapOf("id" to false), Boolean::class.java, false),
        Arguments.of(mapOf("id" to null), Integer::class.java, null),
        Arguments.of(mapOf("id" to "something"), Record::class.java, Record(mapOf("id" to "something"))),
      )
    }
  }
}