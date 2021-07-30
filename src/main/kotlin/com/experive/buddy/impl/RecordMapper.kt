package com.experive.buddy.impl

import com.experive.buddy.Record
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

internal class RecordMapper : RowMapper<Record> {
  private lateinit var columns: List<String>
  fun getColumn(rs: ResultSet): List<String> {
    if (!this::columns.isInitialized) {
      val metaData = rs.metaData
      columns = (0 until metaData.columnCount).map { metaData.getColumnName(it + 1) }
    }
    return this.columns
  }

  override fun mapRow(rs: ResultSet, rowNum: Int): Record {
    return Record(getColumn(rs).mapIndexed { index, name -> name.lowercase() to rs.getObject(index + 1) }.toMap())
  }
}