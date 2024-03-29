@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "UNUSED_PARAMETER")

package retroktor.internal.dsl

import io.ktor.http.*
import retroktor.internal.RetroKtorClientImpl

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun HttpMessageBuilder.header(name: String, value: Any?) {
  if (value != null) headers.append(name, value.toString())
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun HttpMessageBuilder.headers(name: String, values: List<Any?>) {
  if (values.isNotEmpty()) headers.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
@RetroKtorDsl
inline fun HttpMessageBuilder.headers(name: String, values: Array<out Any?>) {
  if (values.isNotEmpty()) headers.appendAll(name, values.mapNotNull { it?.toString() })
}

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("headersMap")
inline fun HttpMessageBuilder.headers(map: Map<String, Any?>) =
  map.forEach { (name, value) -> header(name, value) }

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("headersMapOfList")
inline fun HttpMessageBuilder.headers(map: Map<String, List<Any?>>) =
  map.forEach { (name, value) -> headers(name, value) }

context(RetroKtorClientImpl)
@RetroKtorDsl
@JvmName("headersMapOfArray")
inline fun HttpMessageBuilder.headers(map: Map<String, Array<out Any?>>) =
  map.forEach { (name, value) -> headers(name, value) }
