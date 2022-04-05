package retroktor.internal.dsl

import kotlin.annotation.AnnotationTarget.*

/** DSL marker. */
@DslMarker
@Target(CLASS, TYPEALIAS, TYPE, FUNCTION)
annotation class RetroKtorDsl
