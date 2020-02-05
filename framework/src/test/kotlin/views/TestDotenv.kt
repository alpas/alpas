package dev.alpas.tests.views

import dev.alpas.Environment
import dev.alpas.RunMode
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvEntriesFilter
import io.github.cdimascio.dotenv.DotenvEntry
import java.util.*

open class TestEnv(envVars: List<Pair<String, String>>) : Environment(TestDotenv(envVars), "", "", RunMode.TEST)
class TestDotenv(envVars: List<Pair<String, String>>) : Dotenv() {
    private val map = envVars.associateBy({ it.first }, { it.second })
    private val set: Set<DotenvEntry> =
        Collections.unmodifiableSet(
            buildEnvEntries().map {
                DotenvEntry(
                    it.key,
                    it.value
                )
            }.toSet()
        )

    override fun entries() = set
    override fun entries(filter: DotenvEntriesFilter) = map.map {
        DotenvEntry(
            it.key,
            it.value
        )
    }.toSet()

    override fun get(envName: String): String? = System.getenv(envName) ?: map[envName]

    private fun buildEnvEntries(): Map<String, String> {
        val envMap = map.toMap(mutableMapOf())
        System.getenv().entries.forEach {
            envMap[it.key] = it.value
        }
        return envMap
    }
}
