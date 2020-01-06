package dev.alpas.queue.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp

object JobRecords : LongIdTable("jobs") {
    val queue = varchar("queue", 255).index()
    val payload = text("payload")
    val tries = integer("tries")
    val reservedAt = long("reserved_at").nullable()
    val availableAt = long("available_at")
    val createdAt = long("created_at")
}

class JobRecord(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<JobRecord>(JobRecords)

    var queue by JobRecords.queue
    var payload by JobRecords.payload
    var tries by JobRecords.tries
    var reservedAt by JobRecords.reservedAt
    var availableAt by JobRecords.availableAt
    var createdAt by JobRecords.createdAt
}

object FailedJobRecords : LongIdTable("failed_jobs") {
    val connection = varchar("connection", 255).index()
    val queue = varchar("queue", 255).index()
    val payload = text("payload")
    val exception = text("exception")
    val failedAt = timestamp("failed_at").defaultExpression(CurrentTimestamp())
}

class FailedJobRecord(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FailedJobRecord>(FailedJobRecords)

    var connection by FailedJobRecords.connection
    var queue by FailedJobRecords.queue
    var payload by FailedJobRecords.payload
    var exception by FailedJobRecords.exception
    var failedAt by FailedJobRecords.failedAt
}
