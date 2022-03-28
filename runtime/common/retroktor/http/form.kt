package retroktor.http

import io.ktor.util.converters.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


/**
 * Denotes that the request body will use form URL encoding. Fields should be declared as parameters
 * and annotated with [@Field][Field].
 *
 * Requests made with this annotation will have `application/x-www-form-urlencoded` MIME type.
 *
 * Field names and values will be UTF-8 encoded before being URI-encoded in accordance to
 * [RFC-3986](http://tools.ietf.org/html/rfc3986).
 */
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class FormUrlEncoded


/**
 * Named pair for a form-encoded request.
 *
 * Values are converted to strings using [ConversionService] (or [toString], in the absence of a `ConversionService`)
 * and then form URL encoded. `null` values are ignored.
 *
 * Passing a [List] or [array][Array] (including `varargs`) will result in a field pair for each non-`null` item.
 *
 * ######
 * ### Simple Example:
 * ```
 * @FormUrlEncoded
 * @POST("/")
 * suspend fun example(
 *   @Field("name") String name,
 *   @Field("occupation") String occupation
 * ): HttpResponse
 * ```
 *  Calling with `foo.example("Bob Smith", "President")` yields a request body of `name=Bob+Smith&occupation=President`
 *
 * ######
 * ### Array/Varargs Example:
 * ```
 * @POST("/list")
 * @FormUrlEncoded
 * suspend fun example(@Field("name") vararg names: String): HttpResponse
 * ```
 * Calling with `foo.example("Bob Smith", "Jane Doe")` yields a request body of `name=Bob+Smith&name=Jane+Doe`.
 *
 * @param encoded Specifies whether the [name][value] and value are already URL encoded.
 *
 * @see FormUrlEncoded
 * @see FieldMap
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Field(
  val value: String,
  val encoded: Boolean = false
)


/**
 * Named key/value pairs for a form-encoded request.
 *
 * ######
 * ### Simple Example:
 * ```
 * @FormUrlEncoded
 * @POST("/things")
 * suspend fun things(@FieldMap Map<String, String> fields): HttpResponse
 * ```
 * Calling with `foo.things(mapOf("foo" to "bar", "kit" to "kat")` yields a request body of `foo=bar&kit=kat`.
 *
 * A `null` value for the map, as a key, or as a value is not allowed.
 *
 * @param encoded Specifies whether the names and values are already URL encoded.
 *
 * @see FormUrlEncoded
 * @see Field
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class FieldMap(val encoded: Boolean = false)
