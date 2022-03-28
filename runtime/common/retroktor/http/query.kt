package retroktor.http

import io.ktor.util.converters.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


/**
 * Query parameter appended to the URL.
 *
 * Values are converted to strings using [ConversionService]
 * (or [toString], in the absence of a `ConversionService`) and then URL encoded.
 * `null` values are ignored. Passing a [List][List] or array will result in a
 * query parameter for each non-`null` item.
 *
 * ######
 * ### Simple Example:
 * ```
 * @GET("/friends")
 * suspend fun friends(@Query("page") page: Int): HttpResponse
 * ```
 * Calling with `foo.friends(1)` yields `/friends?page=1`
 *
 * ######
 * ### Example with `null`:
 * ```
 * @GET("/friends")
 * suspend fun friends(@Query("group") group: String?): HttpResponse
 * ```
 * Calling with `foo.friends(null)` yields `/friends`.
 *
 * ######
 * ### Array/Varargs Example:
 * ```
 * @GET("/friends")
 * suspend fun friends(@Query("group") vararg groups: String): HttpResponse
 * ```
 * Calling with `foo.friends("coworker", "bowling")` yields `/friends?group=coworker&group=bowling`.
 *
 * ######
 * Parameter names and values are URL encoded by default. Specify [encoded=true][encoded]
 * to change this behavior.
 * ```
 * @GET("/friends")
 * suspend fun friends(@Query(value="group", encoded=true) group: String): HttpResponse
 * ```
 * Calling with `foo.friends("foo+bar"))` yields `/friends?group=foo+bar`.
 *
 * @param value The query parameter name.
 * @param encoded Specifies whether the parameter [name][value] and value are already URL encoded.
 *
 * @see QueryMap
 * @see QueryName
 */
@MustBeDocumented
@Retention(RUNTIME)
@Target(VALUE_PARAMETER)
annotation class Query(val value: String, val encoded: Boolean = false)


/**
 * Query parameter keys and values appended to the URL.
 *
 * Values are converted to strings using [ConversionService] (or [toString], in the absence of a `ConversionService`)
 *
 * ######
 * ### Simple Example:
 * ```
 * @GET("/friends")
 * suspend fun friends(@QueryMap filters: Map<String, String>): HttpResponse
 * ```
 * Calling with `foo.friends(mapOf("group" to "coworker", "age" to "42"))` yields `/friends?group=coworker&age=42`.
 *
 * ######
 * Map keys and values representing parameter values are URL encoded by default.
 * Specify [encoded=true][encoded] to change this behavior.
 *
 * ```
 * @GET("/friends")
 * suspend fun list(@QueryMap(encoded=true) Map<String, String> filters): HttpResponse
 * ```
 * Calling with `foo.list(mapOf("group" to "coworker+bowling"))` yields `/friends?group=coworker+bowling`.
 *
 * ######
 * A `null` value for the map, as a key, or as a value is not allowed.
 *
 * @param encoded Specifies whether parameter names and values are already URL encoded.
 *
 * @see Query
 * @see QueryName
 */
@MustBeDocumented
@Retention(RUNTIME)
@Target(VALUE_PARAMETER)
annotation class QueryMap(val encoded: Boolean = false)


/**
 * Query parameter appended to the URL that has no value.
 *
 * Passing a [List][List] or array will result in a query parameter for each non-`null` item.
 *
 * ######
 * ### Simple Example:
 * ```
 * @GET("/friends")
 * suspend fun friends(@QueryName filter: String): HttpResponse
 * ```
 * Calling with `foo.friends("contains(Bob)")` yields `/friends?contains(Bob)`.
 *
 * ######
 * ### Array/Varargs Example:
 * ```
 * @GET("/friends")
 * suspend fun friends(@QueryName vararg filters: String): HttpResponse
 * ```
 * Calling with `foo.friends("contains(Bob)", "age(42)")` yields `/friends?contains(Bob)&age(42)`.
 *
 * ######
 * Parameter names are URL encoded by default. Specify [encoded=true][encoded] to change this behavior.
 * ```
 * @GET("/friends")
 * suspend fun friends(@QueryName(encoded=true) filter: String): HttpResponse
 * ```
 * Calling with `foo.friends("name+age"))` yields `/friends?name+age`.
 *
 * @param encoded Specifies whether parameter is already URL encoded.
 *
 * @see Query
 * @see QueryMap
 */
@MustBeDocumented
@Retention(RUNTIME)
@Target(VALUE_PARAMETER)
annotation class QueryName(val encoded: Boolean = false)
