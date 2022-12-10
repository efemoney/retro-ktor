@file:Suppress("NOTHING_TO_INLINE", "unused")

package retroktor.internal.dsl

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import retroktor.internal.RetroKtorClientImpl

@RetroKtorDsl
interface FormUrlBodyBuilder {
  val parameters: ParametersBuilder
  val encodedParameters: ParametersBuilder
}

@PublishedApi
internal class FormUrlBodyBuilderImpl : FormUrlBodyBuilder {
  // Use an internal url builder because [UrlDecodedParametersBuilder] is internal in Ktor
  private val url = URLBuilder()
  override val parameters get() = url.parameters
  override val encodedParameters get() = url.encodedParameters
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun HttpRequestBuilder.formUrlBody(builder: FormUrlBodyBuilder.() -> Unit) {
  setBody(FormDataContent(FormUrlBodyBuilderImpl().apply(builder).encodedParameters.build()))
}


context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun FormUrlBodyBuilder.parameter(name: String, value: Any?) {
  if (value != null) parameters.append(name, value.toString())
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun FormUrlBodyBuilder.parameters(name: String, values: List<Any?>) {
  if (values.isNotEmpty()) parameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun FormUrlBodyBuilder.parameters(name: String, values: Array<out Any?>) {
  if (values.isNotEmpty()) parameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("parametersMap")
inline fun FormUrlBodyBuilder.parameters(map: Map<String, Any?>) =
  map.forEach { (name, value) -> parameter(name, value) }

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("parametersMapOfList")
inline fun FormUrlBodyBuilder.parameters(map: Map<String, List<Any?>>) =
  map.forEach { (name, value) -> parameters(name, value) }

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("parametersMapOfArray")
inline fun FormUrlBodyBuilder.parameters(map: Map<String, Array<out Any?>>) =
  map.forEach { (name, value) -> parameters(name, value) }


context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun FormUrlBodyBuilder.encodedParameter(name: String, value: Any?) {
  if (value != null) encodedParameters.append(name, value.toString())
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun FormUrlBodyBuilder.encodedParameters(name: String, values: List<Any?>) {
  if (values.isNotEmpty()) encodedParameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun FormUrlBodyBuilder.encodedParameters(name: String, values: Array<out Any?>) {
  if (values.isNotEmpty()) encodedParameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("encodedParametersMap")
inline fun FormUrlBodyBuilder.encodedParameters(map: Map<String, Any?>) =
  map.forEach { (name, value) -> encodedParameter(name, value) }

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("encodedParametersMapOfList")
inline fun FormUrlBodyBuilder.encodedParameters(map: Map<String, List<Any?>>) =
  map.forEach { (name, value) -> encodedParameters(name, value) }

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("encodedParametersMapOfArray")
inline fun FormUrlBodyBuilder.encodedParameters(map: Map<String, Array<out Any?>>) =
  map.forEach { (name, value) -> encodedParameters(name, value) }
