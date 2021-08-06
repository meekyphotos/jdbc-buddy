package com.experive.buddy.benchmark

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

typealias Block = () -> Unit

fun benchmark(warmup: Int = 5, iterations: Int = 10, itemsInvolved: Int = 1, cleanUp: Block?, block: Block) {
  if (warmup > 0) {
    println("Warming up")
    for (i in 1..warmup) {
      val nano = measureNanoTime { block() }
      cleanUp?.invoke()
      println("Warm ${i}. ${TimeUnit.NANOSECONDS.toMillis(nano)}ms")
    }
  }
  if (iterations > 0) {
    println("Benchmarking...")
    val ds = DescriptiveStatistics()
    for (i in 1..warmup) {
      val nano = measureNanoTime { block() }
      cleanUp?.invoke()
      ds.addValue(nano.toDouble())

      println("Execution ${i}. ${TimeUnit.NANOSECONDS.toMillis(nano)}ms | ${itemsInvolved / toSeconds(nano)} item/s")
    }
    println("Summary")
    println("Mean execution time: ${TimeUnit.NANOSECONDS.toMillis(ds.mean.toLong())}")
    println("Min execution time: ${TimeUnit.NANOSECONDS.toMillis(ds.min.toLong())}")
    println("Max execution time: ${TimeUnit.NANOSECONDS.toMillis(ds.max.toLong())}")
    println("Skewness: ${TimeUnit.NANOSECONDS.toMillis(ds.skewness.toLong())}")
  }

}

fun toSeconds(nano: Long): Double {
  return nano.toDouble() * 1e-9
}
