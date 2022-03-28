@file:Suppress("NOTHING_TO_INLINE")

package retroktor.internal

import io.ktor.http.*

/** Annotates the top-level class/function in each RetroKtor generated source file. */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RetroKtorGenerated

/** Interface implement by all [RetroKtorGenerated] clients */
interface RetroKtorClientImpl

context(RetroKtorClientImpl) inline fun ParametersBuilder.appendName(name: String) {
  appendAll(name, emptyList())
}

context(RetroKtorClientImpl) inline fun ParametersBuilder.appendNames(names: List<String>) {
  names.forEach { appendName(it) }
}

context(RetroKtorClientImpl) inline fun ParametersBuilder.appendNames(vararg names: String) {
  names.forEach { appendName(it) }
}

context(RetroKtorClientImpl) inline fun ParametersBuilder.appendMap(map: Map<String, String>) {
  map.forEach { (name, value) -> append(name, value) }
}

context(RetroKtorClientImpl) inline fun ParametersBuilder.appendMapList(map: Map<String, Iterable<String>>) {
  map.forEach { (name, value) -> appendAll(name, value) }
}

context(RetroKtorClientImpl) inline fun ParametersBuilder.appendMapArray(map: Map<String, Array<String>>) {
  map.forEach { (name, value) -> appendAll(name, value.toList()) }
}
