package dev.alpas.routing

import dev.alpas.AppConfig
import dev.alpas.toDate
import org.sagebionetworks.url.HttpMethod
import org.sagebionetworks.url.UrlSignerUtils
import uy.klutter.core.uri.buildUri
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.ZonedDateTime

class UrlGenerator(private val root: URI, private val router: Router, private val appConfig: AppConfig) {
    private val paramMatchRegex by lazy { """<([^>]*)>""".toRegex() }

    fun route(
        name: String,
        params: Map<String, Any>? = null,
        absolute: Boolean = true,
        defaultPath: String? = null
    ): String {
        val mutableParamsMap = params?.toMutableMap() ?: mutableMapOf()
        val route = router.findNamedRoute(name)
        if (route == null) {
            // if require that the route is found or that there is a default path, otherwise we throw an exception
            requireNotNull(defaultPath) { "Route $name isn't defined." }
            return defaultPath
        }
        val basePath = route.path.replace(paramMatchRegex) {
            val match = it.groupValues[1]
            val value = mutableParamsMap.remove(match)
                ?: throw IllegalArgumentException("No value provided for required parameter '$match'")
            value.toString()
        }

        val uri = buildUri(root)
            .encodedPath(basePath)
            .addQueryParams(*mutableParamsMap.map { it.key to it.value.toString() }.toTypedArray())
            .build()
            .toURI()
        return (if (absolute) {
            uri
        } else {
            root.relativize(uri)
        }).toString()
    }


    fun url(path: String, params: Map<String, Any> = emptyMap(), forceSecure: Boolean = false): URI {
        return url(root, path, params, forceSecure)
    }

    fun signedRoute(name: String, params: Map<String, Any>? = null, expiration: Duration): URL {
        val url = route(name, params = params, absolute = true)
        return sign(url, expiration)
    }

    fun sign(url: String, expiration: Duration): URL {
        // todo: cleanup UrlSignerUtils
        val expirationDateTime = ZonedDateTime.now(appConfig.timezone).plusMinutes(expiration.toMinutes())
        return UrlSignerUtils.generatePreSignedURL(
            HttpMethod.GET,
            url,
            expirationDateTime.toDate(),
            appConfig.encryptionKey
        )
    }

    fun checkSignature(url: String): Boolean {
        return try {
            val signature = UrlSignerUtils.validatePresignedURL(HttpMethod.GET, url, appConfig.encryptionKey)
            signature != null
        } catch (e: Exception) {
            false
        }
    }

    fun hasRoute(name: String): Boolean {
        return router.findNamedRoute(name) != null
    }
}

fun url(root: URI, path: String = "", params: Map<String, Any> = emptyMap(), forceSecure: Boolean = false): URI {
    val builder = buildUri(root)
        .encodedPath(path)
        .addQueryParams(*params.map { it.key to it.value.toString() }.toTypedArray())
    if (forceSecure) {
        builder.scheme("https://")
    }
    return builder.build().toURI()
}
