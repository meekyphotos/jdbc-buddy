package com.experive.buddy

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

data class TestEntity(
  @Id @GeneratedValue
  var id: Int? = null,
  var name: String? = null,
  var fieldName: Int? = null,
  var booleanField: Boolean? = false
)

@Table()
data class TestJson(
  @Id @GeneratedValue
  var id: Int? = null,
  var map: JsonObject?,
  var relation: JsonArray<Int>?
)

data class TestRelation(
  @Id @GeneratedValue
  var id: Int? = null,
  var testId: Int? = null,
  var active: Boolean? = null
)

data class AliasedTestEntity(
  val userName: String,
  val loginCount: Int
)

@Table(name = "test_entity")
data class ImmutableTestEntity(
  @Id @GeneratedValue
  val id: Int,
  val name: String?,
  val fieldName: Int,
  val booleanField: Boolean
)

@Table(name = "test_entity")
class ForcedEmptyConstructorTestEntity {
  @Id
  @GeneratedValue
  var id: Int? = null
  var name: String? = null
  var fieldName: Int? = null
  var booleanField: Boolean? = null
}