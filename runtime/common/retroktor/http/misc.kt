package retroktor.http

import io.ktor.client.statement.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

/**
 * Treat the response body on methods returning [HttpResponse] as is, i.e.
 * without converting the body to `byte[]`.
 */
@MustBeDocumented
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
annotation class Streaming


/**
 * Adds the argument instance as a request tag using the type as the key.
 *
 * ```
 * @GET("/")
 * suspend foo(@Tag String tag): HttpResponse
 * ```
 *
 * Tag arguments may be `null` which will omit them from the request. Passing a parameterized
 * type such as `List<String>` will use the raw type (i.e., `List.class`) as the key.
 * Duplicate tag types are not allowed.
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Tag
