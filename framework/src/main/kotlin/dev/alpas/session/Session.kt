package dev.alpas.session

import javax.servlet.http.HttpServletRequest

const val CSRF_SESSION_KEY = "_csrf"
const val VALIDATION_ERRORS_KEY = "_validation_errors"
const val OLD_INPUTS_KEY = "_old_inputs"

@Suppress("UNCHECKED_CAST", "unused")
class Session(private val request: HttpServletRequest) {
    private val session = request.session
    operator fun <T> invoke(key: String): T? {
        return get(key)
    }

    operator fun <T> invoke(key: String, default: T): T {
        return get(key, default)
    }

    operator fun <T> invoke(key: String, default: () -> T): T {
        return get(key, default)
    }

    operator fun invoke(map: Map<String, Any>): Map<String, Any> {
        return map.apply {
            forEach { key, value -> session.setAttribute(key, value) }
        }
    }

    operator fun <T> get(key: String): T? {
        return session.getAttribute(key) as? T
    }

    operator fun <T> get(key: String, default: T): T {
        return this.invoke(key) as? T ?: default
    }

    operator fun <T> get(key: String, default: () -> T): T {
        return this.invoke(key) as? T ?: default()
    }

    fun has(key: String) = session.getAttribute(key) != null

    fun exists(key: String) = session.attributeNames.asSequence().contains(key)

    fun put(key: String, value: Any?) {
        session.setAttribute(key, value)
    }

    fun put(values: Map<String, Any?>) {
        values.forEach { (key, value) ->
            put(key, value)
        }
    }

    operator fun set(key: String, value: Any?) {
        session.setAttribute(key, value)
    }

    fun <T> pull(key: String): T? {
        return this.invoke<T?>(key)?.also {
            forget(key)
        }
    }

    fun <T> pull(key: String, default: T): T {
        return this.pull(key) ?: default
    }

    fun <T> pull(key: String, default: () -> T): T {
        return this.pull(key) ?: default()
    }

    fun forget(key: String, vararg keys: String) {
        listOf(key, *keys).forEach {
            session.removeAttribute(it)
        }
    }

    internal fun flush() {
        session.attributeNames.toList().forEach {
            forget(it)
        }
    }

    internal fun nextFlashBag(): MutableMap<String, Any?> {
        return getOrCreate("_next_flash_bag", mutableMapOf()) ?: mutableMapOf()
    }

    private fun previousFlashBag(default: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        return getOrCreate("_previous_flash_bag") {
            default
        } ?: emptyMap()
    }

    internal fun copyPreviousFlashBag() {
        put("_previous_flash_bag", pull("_next_flash_bag"))
    }

    fun flashBag(): Map<String, Any?> {
        return previousFlashBag()
    }

    fun reflash() {
        nextFlashBag().putAll(previousFlashBag())
    }

    fun flash(name: String, payload: Any?) {
        nextFlashBag()[name] = payload
    }

    fun isValid(): Boolean {
        return request.isRequestedSessionIdValid
    }

    internal fun clearPreviousFlashBag() {
        forget("_previous_flash_bag")
    }

    fun invalidate() {
        session.invalidate()
    }

    fun regenerate(flushCurrent: Boolean = false) {
        if (flushCurrent) {
            flush()
        }
        request.changeSessionId()
    }

    fun <T> getOrCreate(key: String, default: () -> T?): T? {
        if (exists(key)) {
            return get(key)
        }
        return default().also {
            put(key, it)
        }
    }

    fun <T> getOrCreate(key: String, default: T?): T? {
        if (exists(key)) {
            return get(key)
        }
        put(key, default)
        return default
    }

    fun csrfToken(): String? {
        return this[CSRF_SESSION_KEY]
    }

    fun savePreviousUrl(url: String) {
        this.put("_previous_url", url)
    }

    fun saveIntendedUrl(url: String) {
        this.put("_intended_url", url)
    }

    fun old(): Map<String, List<Any>> {
        val flash = flashBag()
        return flash[OLD_INPUTS_KEY] as? Map<String, List<Any>> ?: emptyMap()
    }

    fun errors(): Map<String, List<Any>> {
        val flash = flashBag()
        return flash[VALIDATION_ERRORS_KEY] as? Map<String, List<Any>> ?: emptyMap()
    }
}
