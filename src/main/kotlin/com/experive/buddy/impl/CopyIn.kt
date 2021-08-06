package com.experive.buddy.impl

import com.experive.buddy.TableInfo
import com.experive.buddy.mapper.writeJson
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.GenericObjectPool
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import reactor.core.publisher.Flux
import java.util.concurrent.CountDownLatch

fun quote(appendable: Appendable, input: Any?) {
  when (input) {
    is JsonNode -> {
      val jsonString = writeJson(input)
      quoteString(appendable, jsonString)
    }
    is String -> {
      quoteString(appendable, input)
    }
    else -> {
      appendable.append(input.toString())
    }
  }
}

fun quoteString(writer: Appendable, input: String) {
  writer.append('"')
  var occurrenceIndex: Int = input.indexOf('"', 0)
  if (occurrenceIndex == -1) {
    // fast path = no match
    writer.append(input)
  } else {
    var lastIndex = 0
    while (occurrenceIndex != -1) {
      writer.append(input.subSequence(lastIndex, occurrenceIndex))
      writer.append("\"\"")
      lastIndex = occurrenceIndex + 1
      occurrenceIndex = input.indexOf('"', occurrenceIndex + 1)
    }
    writer.append(input.subSequence(lastIndex, input.length))
  }
  writer.append('"')
}

class CopyIn<R : Any>(private val template: JdbcTemplate, private val entityClass: TableInfo<R>) {

  fun execute(values: Flux<R>) {
    template.execute(ConnectionCallback { conn ->
      val cm = CopyManager(conn.unwrap(BaseConnection::class.java))
      val sb = StringBuilder()
      sb.append("COPY ${entityClass.name()} (")
      val columns = entityClass.insertableColumns()
      columns.joinTo(sb, ",") { it.name }
      sb.append(") FROM STDIN WITH (FORMAT csv)")
      val copyIn = cm.copyIn(sb.toString())
      val size = columns.size
      val buffers = GenericObjectPool(object : BasePooledObjectFactory<StringBuilder>() {
        override fun create(): StringBuilder = StringBuilder(51200)
        override fun wrap(obj: StringBuilder?): PooledObject<StringBuilder> = DefaultPooledObject(obj)
      })
      val stringBuilder = StringBuilder(512000)
      val countDownLatch = CountDownLatch(1)
      values
        .map { r ->
          val line = buffers.borrowObject()
          try {
            line.setLength(0)
            var i = 0
            while (i < size) {
              quote(line, columns[i].getValue(r))
              if (i + 1 < size) {
                line.append(',')
              }
              i++
            }
            line.append('\n')

            line.toString()
          } finally {
            buffers.returnObject(line)
          }
        }
        .buffer(10000)
        .subscribe({
          stringBuilder.setLength(0)
          it.joinTo(stringBuilder, "")
          val linesToWrite = stringBuilder.toString().toByteArray()
          copyIn.writeToCopy(linesToWrite, 0, linesToWrite.size)
          copyIn.flushCopy()
        }, null) {
          copyIn.endCopy()
          countDownLatch.countDown()
        }

      countDownLatch.await()
    })

  }
}