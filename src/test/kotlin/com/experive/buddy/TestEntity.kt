package com.experive.buddy

import com.experive.buddy.annotations.Id
import com.experive.buddy.annotations.Table

data class TestEntity(
  @Id
  var id: Int? = null,
  var name: String? = null,
  var fieldName: Int? = null,
  var booleanField: Boolean? = false
)

data class TestRelation(
  @Id
  var id: Int? = null,
  var testId: Int? = null,
  var active: Boolean? = null
)

data class AliasedTestEntity(
  val userName: String,
  val loginCount: Int
)

@Table("test_entity")
data class ImmutableTestEntity(
  @Id
  val id: Int,
  val name: String?,
  val fieldName: Int,
  val booleanField: Boolean
)

@Table("test_entity")
class ForcedEmptyConstructorTestEntity {
  @Id
  var id: Int? = null
  var name: String? = null
  var fieldName: Int? = null
  var booleanField: Boolean? = null
}