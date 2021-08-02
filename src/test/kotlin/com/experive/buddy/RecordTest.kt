package com.experive.buddy

import com.google.common.truth.Truth
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KClass

internal class RecordTest {

  @ParameterizedTest
  @MethodSource("conversions")
  internal fun shouldConvert(map: Map<String, Any>, clazz: KClass<*>, expectedValue: Any?) {
    val r = Record(map)
    Truth.assertThat(r.into(clazz)).isEqualTo(expectedValue)
  }


  companion object {
    @JvmStatic
    fun conversions(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(mapOf("id" to 1), Int::class, 1),
        Arguments.of(mapOf("id" to 1.0), Double::class, 1.0),
        Arguments.of(mapOf("id" to 1L), Long::class, 1L),
        Arguments.of(mapOf("id" to 1.0f), Float::class, 1.0f),
        Arguments.of(mapOf("id" to false), Boolean::class, false),
        Arguments.of(mapOf("id" to null), Integer::class, null),
        Arguments.of(mapOf("id" to "something"), Record::class, Record(mapOf("id" to "something"))),
      )
    }
  }
}