package retroktor.http

import io.ktor.client.plugins.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*


/**
 * Use a custom HTTP verb for a request.
 *
 * ```
 * interface Service {
 *
 *   @HTTP(method = "CUSTOM", path = "custom/endpoint/")
 *   suspend fun customEndpoint(): HttpResponse
 * }
 * ```
 *
 * This annotation can also be used for sending `DELETE` with a request body:
 * ```
 * interface Service {
 *
 *   @HTTP(method = "DELETE", path = "remove/", hasBody = true)
 *   suspend fun deleteObject(@Body object: OutgoingContent): HttpResponse
 * }
 * ```
 *
 * @param path A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class HTTP(
  val method: String,
  val path: String = "",
  val hasBody: Boolean = false
)


/**
 * Make an OPTIONS request
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class OPTIONS(val value: String = "")


/**
 * Make a HEAD request.
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class HEAD(val value: String = "")


/**
 * Make a GET request.
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url].
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class GET(val value: String = "")


/**
 * Make a POST request.
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
annotation class POST(val value: String = "")


/**
 * Make a PUT request.
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
annotation class PUT(val value: String = "")


/**
 * Make a PATCH request.
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
annotation class PATCH(val value: String = "")


/**
 * Make a DELETE request.
 *
 * @param value A relative or absolute path, or full URL of the endpoint that will be resolved against
 * the [default request][DefaultRequest.DefaultRequestBuilder.url]
 *
 * This value is optional if the first parameter of the method is annotated with [@Url][Url].
 */
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class DELETE(val value: String = "")
