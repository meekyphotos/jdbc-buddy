package com.experive.buddy.support

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import javax.sql.DataSource

abstract class GenericExtension : BeforeEachCallback, AfterEachCallback, BeforeAllCallback {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var txManager: JdbcTransactionManager
    private lateinit var tx: TransactionStatus
    override fun beforeEach(context: ExtensionContext?) {
        context?.testInstance?.ifPresent {
            it.javaClass.fields.filter { f ->
                f.type.isAssignableFrom(JdbcTemplate::class.java)
            }.forEach { f ->
                f.isAccessible = true
                f.set(it, jdbcTemplate)
            }
        }
        tx = txManager.getTransaction(TransactionDefinition.withDefaults())
    }

    override fun afterEach(context: ExtensionContext?) {
        txManager.rollback(tx)
    }

    protected abstract fun dataSource(): DataSource
    override fun beforeAll(context: ExtensionContext?) {
        val dataSource = dataSource()
        txManager = JdbcTransactionManager(dataSource)
        jdbcTemplate = JdbcTemplate(dataSource)
    }
}
