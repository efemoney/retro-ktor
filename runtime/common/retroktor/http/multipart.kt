package retroktor.http

import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.content.*
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


/**
 * Denotes that the request body is multipart. Parts should be declared as parameters and annotated with [@Part][Part].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Multipart


/**
 * Denotes a single part of a multipart request.
 *
 * The parameter type on which this annotation exists will be processed in one of two ways:
 *
 * - If the type is [PartData] or [FormPart] the contents will be used directly.
 *   Omit the name from the annotation (i.e. `@Part part: FormPart<*>`).
 *
 * - If the type is any of the types supported by [FormBuilder.append] the value will be used in a form builder.
 *   See [FormBuilder.append] and [FormPart.value] documentation for the supported types.
 *   Supply the part name in the annotation (e.g. `@Part("foo") foo: String`).
 *
 * Values may be `null` which will omit them from the request body.
 *
 * ````
 * @Multipart
 * @POST("/")
 * suspend fun example(
 *   @Part("description") description: String,
 *   @Part("image") image: InputProvider,
 * ): HttpResponse
 * ```
 *
 * Part parameters may not be `null`.
 *
 * @param value The name of the part. Required for all parameter types except [FormPart] or [PartData].
 * @param encoding The `Content-Transfer-Encoding` of this part.
 */
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Part(
  val value: String = "",
  val encoding: String = "binary",
)


/**
 * Denotes name and value parts of a multipart request.
 *
 * Values of the map on which this annotation exists will be processed in one of two ways:
 *
 * If the type is [HttpResponse] the value will be used directly with its content type.
 * Other object types will be converted to an appropriate representation by using [].
 *
 * ```
 * @Multipart
 * @POST("/upload")
 * Call<ResponseBody> upload(
 * @Part("file") RequestBody file,
 * @PartMap Map<String, RequestBody> params);
 * ```
 *
 * A `null` value for the map, as a key, or as a value is not allowed.
 *
 * @param encoding The `Content-Transfer-Encoding` of the parts.
 *
 * @see Multipart
 * @see Part
 */
@MustBeDocumented
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class PartMap(val encoding: String = "binary")
