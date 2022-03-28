package retroktor.http

import io.ktor.util.converters.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import io.ktor.http.Headers as KtorHeaders


/**
 * Adds headers supplied literally in the `value`.
 *
 * ```
 * @Headers("Cache-Control: max-age=640000")
 * @GET("/")
 *
 * @Headers(
 *   "X-Foo: Bar",
 *   "X-Ping: Pong",
 * )
 * @GET("/")
 * ```
 *
 * **Note:** Headers do not overwrite each other. All headers with the same name will
 * be included in the request.
 *
 * @see Header
 * @see HeaderMap
 */
@Target(FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Headers(vararg val value: String)


/**
 * Replaces the header with the value of its target.
 * ```
 * @GET("/")
 * suspend fun foo(@Header("Accept-Language") lang: String): HttpResponse
 * ```
 *
 * Header parameters may be `null` which will omit them from the request.
 * Passing a [List] or [array][Array] will result in a header for each non-`null` item.
 *
 * **Note:** Headers do not overwrite each other. All headers with the same name will be included in the request.
 *
 * @see Headers
 * @see HeaderMap
 */
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Header(val value: String)


/**
 * Adds headers specified in the [Map] or [Headers][KtorHeaders].
 *
 * Values in the map are converted to strings using [ConversionService]
 * (or [toString], in the absence of a `ConversionService`).
 *
 * ######
 * ### Simple Example:
 * ```
 * @GET("/search")
 * suspend fun list(@HeaderMap headers: Map<String, String>)
 *
 * // The following call yields /search with headers
 * // Accept: text/plain and Accept-Charset: utf-8
 * foo.list(mapOf("Accept" to "text/plain", "Accept-Charset" to "utf-8"))
 * ```
 *
 * @see Header
 * @see Headers
 */
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@MustBeDocumented
annotation class HeaderMap
