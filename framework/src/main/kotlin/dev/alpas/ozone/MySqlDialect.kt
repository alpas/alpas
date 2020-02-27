@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package dev.alpas.ozone

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.Query
import me.liuwj.ktorm.dsl.asc
import me.liuwj.ktorm.dsl.limit
import me.liuwj.ktorm.dsl.orderBy
import me.liuwj.ktorm.entity.EntitySequence
import me.liuwj.ktorm.entity.asSequence
import me.liuwj.ktorm.entity.sortedBy
import me.liuwj.ktorm.entity.take
import me.liuwj.ktorm.expression.QueryExpression
import me.liuwj.ktorm.expression.SqlFormatter
import me.liuwj.ktorm.support.mysql.MySqlDialect
import me.liuwj.ktorm.support.mysql.MySqlFormatter
import me.liuwj.ktorm.support.mysql.rand

internal class CustomMySqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int) :
    MySqlFormatter(database, beautifySql, indentSize) {

    override fun visitQuery(expr: QueryExpression): QueryExpression {
        super.visitQuery(expr)

        if ("forUpdate" in expr.extraProperties) {
            write("for update ");
        }

        return expr
    }
}

@Suppress("unused")
class MySqlDialect : MySqlDialect() {
    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
        return CustomMySqlFormatter(database, beautifySql, indentSize)
    }
}

/**
 * Return random [count] number of entity sequence.
 *
 * This method is only available for MySQL dialect. Also, it is not optimized for speed so performance
 * is not great. It is recommended to rarely use this method. Such as using it in a database seeder.
 *
 * @param count The number of entities to "take". Default is set to 1.
 *
 * @return A random entity sequence.
 */
inline fun <E : OzoneEntity<E>> OzoneTable<E>.inRandomOrder(count: Int = 1): EntitySequence<E, OzoneTable<E>> {
    return asSequence().sortedBy { rand() }.take(count)
}

/**
 * Apply randomness to this [Query] and limit the result to the given [count].
 *
 * This method is only available for MySQL dialect. Also, it is not optimized for speed so performance
 * is not great. It is recommended to rarely use this method. Such as using it in a database seeder.
 *
 * @param count The number of entities to "limit". Default is set to 1.
 *
 * @return The [Query] itself.
 */
fun Query.inRandomOrder(count: Int = 1) = apply { orderBy(rand().asc()).limit(0, count) }
