package com.experive.buddy.support

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource


class BuddyPostgresExtension : GenericExtension(), AfterAllCallback {
  private val container = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest"))
  override fun dataSource(): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = container.jdbcUrl
    config.username = container.username
    config.password = container.password
    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    return HikariDataSource(config)
  }


  override fun beforeAll(context: ExtensionContext) {
    println("Starting docker container")
    container.withLogConsumer {
      val message = it.utf8String.trim()
      if (message.isNotBlank())
        println(message)
    }
    container.start()
    super.beforeAll(context)
  }

  override fun afterAll(context: ExtensionContext) {
    container.stop()
  }
}