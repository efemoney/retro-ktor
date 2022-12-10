@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "UNUSED_PARAMETER")

package retroktor.internal.dsl

import io.ktor.http.*
import retroktor.internal.RetroKtorClientImpl

context(RetroKtorClientImpl)
inline fun ParametersBuilder.appendName(name: String) {
  appendAll(name, emptyList())
}

context(RetroKtorClientImpl)
inline fun ParametersBuilder.appendNames(names: List<String>) {
  names.forEach { appendName(it) }
}

context(RetroKtorClientImpl)
inline fun ParametersBuilder.appendNames(names: Array<String>) {
  names.forEach { appendName(it) }
}

context(RetroKtorClientImpl)
@JvmName("appendMap")
inline fun ParametersBuilder.appendAll(map: Map<String, String>) {
  map.forEach { (name, value) -> append(name, value) }
}

context(RetroKtorClientImpl)
@JvmName("appendMapOfList")
inline fun ParametersBuilder.appendAll(map: Map<String, Iterable<String>>) {
  map.forEach { (name, value) -> appendAll(name, value) }
}

context(RetroKtorClientImpl)
@JvmName("appendMapOfArray")
inline fun ParametersBuilder.appendAll(map: Map<String, Array<String>>) {
  map.forEach { (name, value) -> appendAll(name, value.toList()) }
}

