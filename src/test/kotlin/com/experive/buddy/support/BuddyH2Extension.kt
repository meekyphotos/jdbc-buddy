package com.experive.buddy.support

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

class BuddyH2Extension : GenericExtension() {
    override fun dataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:testdb;MODE=PostgreSQL"
        config.username = "sa"
        config.password = "password"
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return HikariDataSource(config)
    }
}
