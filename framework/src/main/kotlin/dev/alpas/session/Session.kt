package dev.alpas.session

import javax.servlet.http.HttpServletRequest

const val csrfSessionKey = "_csrf"

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

    fun has(key: String) = session.getAttribute(key) != null

    fun exists(key: String) = session.attributeNames.asSequence().contains(key)

    fun put(key: String, value: Any?) {
        session.setAttribute(key, value)
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

    fun forget(vararg key: String) {
        key.forEach {
            session.removeAttribute(it)
        }
    }

    internal fun flush() {
        session.attributeNames.toList().forEach {
            forget(it)
        }
    }

    internal fun nextFlashBag(): MutableMap<String, Any?> {
        return getOrCreate("_next_flash_bag") {
            mutableMapOf()
        }
    }

    private fun previousFlashBag(default: Map<String, Any?>? = null): Map<String, Any?> {
        return getOrCreate("_previous_flash_bag") {
            default ?: mapOf()
        }
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

    operator fun <T> get(key: String): T? {
        return session.getAttribute(key) as? T
    }

    operator fun <T> get(key: String, default: T): T {
        return this.invoke(key) as? T ?: default
    }

    operator fun <T> get(key: String, default: () -> T): T {
        return this.invoke(key) as? T ?: default()
    }

    fun <T> getOrCreate(key: String, default: () -> T): T {
        return this.invoke(key) as? T ?: default().also {
            put(key, it)
        }
    }

    fun csrfToken(): String? {
        return this[csrfSessionKey]
    }

    fun savePreviousUrl(url: String) {
        this.put("_previous_url", url)
    }

    fun saveIntendedUrl(url: String) {
        this.put("_intended_url", url)
    }

    fun flash(name: String, payload: Any?) {
        nextFlashBag()[name] = payload
    }
}
