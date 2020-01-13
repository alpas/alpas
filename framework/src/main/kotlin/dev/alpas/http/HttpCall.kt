package dev.alpas.http

import dev.alpas.*
import dev.alpas.auth.AuthChannel
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.Authenticatable
import dev.alpas.auth.UserProvider
import dev.alpas.exceptions.ExceptionHandler
import dev.alpas.exceptions.HttpException
import dev.alpas.exceptions.ValidationException
import dev.alpas.routing.RouteResult
import dev.alpas.routing.UrlGenerator
import dev.alpas.validation.ErrorBag
import dev.alpas.validation.Rule
import dev.alpas.validation.ValidationGuard
import mu.KotlinLogging
import org.eclipse.jetty.http.HttpStatus
import uy.klutter.core.uri.buildUri
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Suppress("unused")
class HttpCall internal constructor(
    private val container: Container,
    private val request: Requestable,
    private val response: Responsable,
    internal val route: RouteResult
) : Container by container,
    Requestable by request,
    Responsable by response,
    RequestParamsBagContract by RequestParamsBag(request, route) {

    internal constructor(container: Container, req: HttpServletRequest, res: HttpServletResponse, route: RouteResult)
            : this(container, Request(req), Response(res), route)

    val logger by lazy { KotlinLogging.logger {} }
    var isDropped = false
        private set

    private val exceptionHandler by lazy { makeElse { ExceptionHandler() } }
    val authChannel: AuthChannel by lazy { config<AuthConfig>().channel(this, route.target().authChannel) }
    internal val userProvider: UserProvider? by lazy { authChannel.userProvider }
    val isAuthenticated by lazy { authChannel.isLoggedIn() }
    val isFromGuest by lazy { !isAuthenticated }
    val user: Authenticatable by lazy { authChannel.user.orAbort() }
    val env by lazy { make<Environment>() }
    val redirector by lazy { Redirector(request, response, urlGenerator) }
    val urlGenerator: UrlGenerator by lazy { container.make<UrlGenerator>() }

    init {
        singleton(UrlGenerator(buildUri(request.rootUrl).toURI(), make(), make()))
    }

    fun isSigned(): Boolean {
        return urlGenerator.checkSignature(fullUrl)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Authenticatable> caller(): T {
        return user as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Authenticatable> callerId(): Long {
        return caller<T>().id()
    }

    fun close() {
        if (!jettyRequest.isHandled) {
            response.finalize(this)
        }

        // By this time we have sent a response back to the client and now we need to clear the
        // previous flash messages to avoid showing these messages again in the next request.
        if (sessionIsValid()) {
            session.clearPreviousFlashBag()
        }

        jettyRequest.isHandled = true
    }

    fun applyRules(attribute: String, failfast: Boolean = false, rules: ValidationGuard.() -> Unit): HttpCall {
        ValidationGuard(failfast).also {
            it.call = this
            it.rules()
            it.validate(attribute, errorBag)
            if (it.shouldFailFast) {
                checkValidationErrors { errorBag ->
                    it.handleError(errorBag)
                }
            }
        }
        return this
    }

    fun applyRules(rules: Map<String, Iterable<Rule>>, failfast: Boolean = false): HttpCall {
        ValidationGuard(failfast).also {
            it.call = this
            it.validate(rules, errorBag)
            checkValidationErrors { errorBag ->
                it.handleError(errorBag)
            }
        }
        return this
    }

    fun validate() {
        checkValidationErrors()
    }

    fun <T : ValidationGuard> validateUsing(validator: KClass<out T>, afterSuccessBlock: T.() -> Unit = {}): T {
        return validator.createInstance().also {
            it.call = this
            it.validate(errorBag)
            checkValidationErrors { errorBag ->
                it.handleError(errorBag)
            }
            if (errorBag.isEmpty()) {
                it.afterSuccessfulValidation()
                it.afterSuccessBlock()
            }
        }
    }

    private fun checkValidationErrors(onErrorHandler: (ErrorBag) -> Boolean = fun(_: ErrorBag) = false) {
        if (!errorBag.isEmpty()) {
            if (!onErrorHandler(errorBag)) {
                throw ValidationException(errorBag = errorBag)
            }
        }
    }

    fun routeNamed(name: String, params: Map<String, Any>? = null, absolute: Boolean = true): String {
        return urlGenerator.route(name, params, absolute)
    }

    fun timezone(): ZoneOffset {
        return make<AppConfig>().timezone
    }

    fun nowInCurrentTimezone(): ZonedDateTime {
        return ZonedDateTime.now(timezone())
    }

    fun redirect(
        to: String,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    ) {
        redirect().to(to, status, headers)
    }

    fun redirect(): Redirectable {
        return redirector
    }

    private fun handleException(e: Throwable) {
        // Check if the top layer is an HTTP exception type since most of the times the exception is thrown from
        // a controller, the controller will be wrapped in an InvocationTargetException object. Hence, we'd have to
        // check the cause of this exception to see whether the cause is an actual HTTP exception or not.
        if (HttpException::class.java.isAssignableFrom(e::class.java)) {
            val exceptionHandler = exceptionHandler
            exceptionHandler.handle(e as HttpException, this)
            return
        } else {
            e.cause?.let {
                return handleException(it)
            }
        }
        exceptionHandler.handle(e, this)
        return
    }

    fun drop(e: Exception) {
        isDropped = true
        handleException(e)
        close()
    }

    internal fun sessionIsValid() = env.supportsSession && !servletResponse.isCommitted && session.isValid()

    operator fun <T> invoke(block: HttpCall.() -> T): T {
        return this.block()
    }
}
