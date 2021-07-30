package com.experive.buddy.impl

import com.experive.buddy.Record
import com.experive.buddy.dialect.Dialect
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

internal class RecordMapper(private val dialect: Dialect) : RowMapper<Record> {
  private lateinit var columns: List<String>
  private lateinit var dataTypes: Map<Int, Int>
  fun getColumn(rs: ResultSet): List<String> {
    if (!this::columns.isInitialized) {
      val metaData = rs.metaData
      columns = (0 until metaData.columnCount).map { metaData.getColumnName(it + 1) }
      dataTypes = (0 until metaData.columnCount).associate { (it + 1) to metaData.getColumnType(it + 1) }
    }
    return this.columns
  }

  override fun mapRow(rs: ResultSet, rowNum: Int): Record {
    return Record(getColumn(rs)
      .mapIndexed { index, name -> name.lowercase() to dialect.read(dataTypes, rs, index + 1) }
      .filter { it.second != null }
      .associate { it.first to it.second!! })
  }
}