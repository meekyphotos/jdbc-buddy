
This is a simple SQL generator, uses Spring to manage execution and transactions via JdbcTemplate. Jdbc buddy leverages Kotlin idiomatic syntax to allow you to construct SQL leaving no chance to mistypings or problems arising from db entity
refactorings.

# Notes

## Database supported

It currently supports:

- H2
- Postgres

## This is not an ORM!

While you can use annotated entities to enable some reflection magic, this is not intended to be used as fully fledged ORM. What's missing:

- There is no way to retrieve complex fields: i.e. Collections of related objects, one to one objects
- There is no assistance in creating and maintaining tables

# Getting started

## Add dependency

This package is hosted here on github.com, there add to your repositories section the following:

```xml

<repository>
  <id>github</id>
  <url>https://maven.pkg.github.com/meekyphotos/*</url>
</repository>
```

Then in your dependencies:

```xml

<dependency>
  <groupId>com.experive</groupId>
  <artifactId>jdbc-buddy</artifactId>
  <version>LATEST VERSION</version>
</dependency>
```

To get the latest version, check the release menu

## How to query

```kotlin
data class MyEntity(@Id val id: Int, val name: String?)

fun main() {
    // val jdbcTemplate: JdbcTemplate = ...
    // instantiate or reference a repository
    val repo = Database.using(jdbcTemplate)

    // get reference of the table
    val table = MyEntity::class.table()

    // get reference of the column
    val nameField = table.column(MyEntity::name)

    // prepare and execute your query
    val result = repo
        .selectFrom(table)
        .where(nameField eq "hello")
        .fetchInto()

    // do something with results
}

```

