package retroktor.http

import io.ktor.client.plugins.*
import io.ktor.util.converters.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


/**
 * URL to be resolved against the [default request][DefaultRequest.DefaultRequestBuilder.url]
 * ```
 * @GET
 * suspend fun list(@Url url: String): HttpResponse
 * ```
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Url


/**
 * Named replacement in a URL path segment. Values are converted to strings using [ConversionService] (or [toString],
 * in the absence of a `ConversionService`) and then URL encoded.
 *
 * ######
 * ### Simple example:
 * ```
 * @GET("/image/{id}")
 * suspend fun example(@Path("id") id: Int): HttpResponse
 * ```
 * Calling with `foo.example(1)` yields `/image/1`.
 *
 * ######
 * Values are URL encoded by default. Disable with `encoded=true`.
 * ```
 * @GET("/user/{name}")
 * suspend fun encoded(@Path("name") name: String): HttpResponse
 *
 * @GET("/user/{name}")
 * suspend fun notEncoded(@Path(value="name", encoded=true) name: String): HttpResponse
 * ```
 * Calling `foo.encoded("John+Doe")` yields `/user/John%2BDoe` whereas
 * `foo.notEncoded("John+Doe")` yields `/user/John+Doe`.
 *
 * ######
 * Path parameters may not be `null`.
 *
 * @param encoded Specifies whether the argument value to the annotated method parameter is already URL encoded.
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Path(val value: String, val encoded: Boolean = false)


/**
 * Use this annotation on a service method param when you want to directly control the request body
 * of a POST/PUT request (instead of sending in as request parameters or form-style request body).
 *
 * `Body` parameters may not be `null`.
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Body
