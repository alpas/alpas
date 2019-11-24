@file:Suppress("unused")

package dev.alpas

import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.mordant.TermColors
import org.sagebionetworks.url.UrlData
import uy.klutter.core.common.mustStartWith
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLDecoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletResponse

inline fun String?.ifNotBlank(defaultValue: (String) -> String): String =
    if (!isNullOrBlank()) defaultValue(this!!) else ""

fun LocalDateTime.format(pattern: String): String? {
    return format(DateTimeFormatter.ofPattern(pattern))
}

fun String.now(): String? {
    return LocalDateTime.now().format(this)
}

fun String.asFile() = File(this)

fun HttpServletResponse.charset(): Charset = try {
    Charset.forName(characterEncoding)
} catch (e: Exception) {
    Charset.defaultCharset()
}

fun secureByteArray(size: Int): ByteArray {
    return ByteArray(size).apply {
        SecureRandom().nextBytes(this)
    }
}

fun ByteArray.base64Encoded(): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(this)
}

fun String.base64Encoded(): String {
    return this.toByteArray(Charset.defaultCharset()).base64Encoded()
}

fun String.base64Decoded(): String {
    return this.base64DecodedBytes().toString(Charset.defaultCharset())
}

fun String.base64DecodedBytes(): ByteArray {
    return Base64.getUrlDecoder().decode(this)
}

fun <T> T?.isOneOf(item: T?, vararg items: T): Boolean {
    if (this == item) return true
    return items.isNotEmpty() && items.contains(this)
}

fun String.urlDecode(): String {
    return URLDecoder.decode(this, "UTF-8")
}

fun secureRandomString(length: Int = 16): String {
    return secureByteArray(length).base64Encoded()
}

fun ZonedDateTime.toDate(): Date {
    return Date.from(this.toInstant())
}

fun Any?.printAsError() {
    echo(TermColors().red(this.toString()))
}

fun Any?.printAsSuccess() {
    echo(TermColors().green(this.toString()))
}

fun Any?.printAsWarning() {
    echo(TermColors().yellow(this.toString()))
}

fun Any?.printAsInfo() {
    echo(TermColors().blue(this.toString()))
}

val Throwable.stackTraceString: String
    get() {
        StringWriter().use { sw ->
            PrintWriter(sw).use { pw ->
                printStackTrace(pw)
                return sw.toString()
            }
        }
    }

@ExperimentalUnsignedTypes
fun String.md5(): String {
    return MessageDigest.getInstance("MD5").let {
        it.update(this.toByteArray())
        it.digest()
    }.toHexString()
}

fun Any.toJson(wrapItWith: String? = null): String {
    return if (wrapItWith != null) {
        JsonSerializer.serialize(mapOf(wrapItWith to this))
    } else {
        JsonSerializer.serialize(this)
    }
}

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

@ExperimentalUnsignedTypes
fun String.sign(secret: String, algo: String = "HmacSHA256"): String {
    return Mac.getInstance(algo).let {
        it.init(SecretKeySpec(secret.toByteArray(), algo))
        it.doFinal(toByteArray())
    }.toHexString()
}

fun String?.parseQueryParams(): Map<String, String> {
    return UrlData.parseQueryString(this)
}

/**
 * When something isn't null do something, kinda the opposite of ?:
 */
inline infix fun <T : Any, R : Any> T?.whenNotNull(thenDo: (T) -> R?): R? = if (this == null) null else thenDo(this)

inline infix fun String?.whenNotNullOrEmpty(thenDo: (String) -> String?): String? =
    if (this.isNullOrEmpty()) this else thenDo(this)

/**
 * When something isn't null do something, kinda the opposite of ?:
 */
inline infix fun <T : Any, R : Any> T?.withNotNull(thenDo: T.() -> R?): R? = this?.thenDo()

fun String.mustStartWith(prefix: Char): String {
    return mustStartWith(prefix)
}

fun String.mustStartWith(prefix: String): String {
    return mustStartWith(prefix)
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>
