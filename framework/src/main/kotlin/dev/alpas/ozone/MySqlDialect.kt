package dev.alpas.ozone

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.expression.QueryExpression
import me.liuwj.ktorm.expression.SqlFormatter
import me.liuwj.ktorm.support.mysql.MySqlDialect
import me.liuwj.ktorm.support.mysql.MySqlFormatter

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
