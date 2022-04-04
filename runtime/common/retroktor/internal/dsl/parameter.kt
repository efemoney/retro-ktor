@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "UNUSED_PARAMETER")

package retroktor.internal.dsl

import io.ktor.client.request.*
import retroktor.internal.RetroKtorClientImpl


context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.parameter(name: String, value: Any?) {
  if (value != null) url.parameters.append(name, value.toString())
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.parameters(name: String, values: List<Any?>) {
  if (values.isNotEmpty()) url.parameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.parameters(name: String, values: Array<out Any?>) {
  if (values.isNotEmpty()) url.parameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
  @JvmName("parametersMap")
  inline fun HttpRequestBuilder.parameters(map: Map<String, Any?>) =
  map.forEach { (name, value) -> parameter(name, value) }

context(RetroKtorClientImpl)
  @JvmName("parametersMapOfList")
  inline fun HttpRequestBuilder.parameters(map: Map<String, List<Any?>>) =
  map.forEach { (name, value) -> parameters(name, value) }

context(RetroKtorClientImpl)
  @JvmName("parametersMapOfArray")
  inline fun HttpRequestBuilder.parameters(map: Map<String, Array<out Any?>>) =
  map.forEach { (name, value) -> parameters(name, value) }

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.parameterName(name: String?) {
  if (name != null) url.parameters.appendAll(name, emptyList())
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.parameterNames(names: List<String?>) {
  names.forEach { parameterName(it) }
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.parameterNames(names: Array<out String?>) {
  names.forEach { parameterName(it) }
}


context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.encodedParameter(name: String, value: Any?) {
  if (value != null) url.encodedParameters.append(name, value.toString())
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.encodedParameters(name: String, values: List<Any?>) {
  if (values.isNotEmpty()) url.encodedParameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.encodedParameters(name: String, values: Array<out Any?>) {
  if (values.isNotEmpty()) url.encodedParameters.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
  @JvmName("encodedParametersMap")
  inline fun HttpRequestBuilder.encodedParameters(map: Map<String, Any?>) =
  map.forEach { (name, value) -> encodedParameter(name, value) }

context(RetroKtorClientImpl)
  @JvmName("encodedParametersMapOfList")
  inline fun HttpRequestBuilder.encodedParameters(map: Map<String, List<Any?>>) =
  map.forEach { (name, value) -> encodedParameters(name, value) }

context(RetroKtorClientImpl)
  @JvmName("encodedParametersMapOfArray")
  inline fun HttpRequestBuilder.encodedParameters(map: Map<String, Array<out Any?>>) =
  map.forEach { (name, value) -> encodedParameters(name, value) }

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.encodedParameterName(name: String?) {
  if (name != null) url.encodedParameters.appendAll(name, emptyList())
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.encodedParameterNames(names: List<String?>) {
  names.forEach { encodedParameterName(it) }
}

context(RetroKtorClientImpl)
  inline fun HttpRequestBuilder.encodedParameterNames(names: Array<out String?>) {
  names.forEach { encodedParameterName(it) }
}
