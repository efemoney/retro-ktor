package retroktor.internal

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/** Annotates all RetroKtor generated top-level classes or functions. */
@MustBeDocumented
@Target(CLASS, FUNCTION)
@Retention(BINARY)
annotation class RetroKtorGenerated

/** Interface implemented by all [RetroKtorGenerated] clients. */
interface RetroKtorClientImpl
