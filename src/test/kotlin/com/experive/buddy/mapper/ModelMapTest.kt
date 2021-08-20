package com.experive.buddy.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

data class Source(val id: Long, val name: String)
data class InvalidSource(val id: Boolean, val name: String)
data class Destination(val id: Int?, val name: String)

internal class ModelMapTest {

    @Test
    internal fun shouldMapBetweenCompatibleEntities() {
        val underTest = ModelMap(Destination::class)
        val dst = underTest.map(Source(1L, "hello"))
        assertThat(dst).isEqualTo(Destination(1, "hello"))
    }

    @Test
    internal fun shouldMapBetweenEntities() {
        val underTest = ModelMap(Destination::class)
        val dst = underTest.map(Destination(1, "hello"))
        assertThat(dst).isEqualTo(Destination(1, "hello"))
    }

    @Test
    internal fun invalidValuesAreMappedToNull() {
        val underTest = ModelMap(Destination::class)
        val dst = underTest.map(InvalidSource(false, "hello"))
        assertThat(dst).isEqualTo(Destination(null, "hello"))
    }
}
