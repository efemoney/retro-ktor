package retroktor.internal

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/** Annotates the top-level class/function in each RetroKtor generated source file. */
@MustBeDocumented
@Target(CLASS, FUNCTION)
@Retention(BINARY)
annotation class RetroKtorGenerated

/** Interface implement by all [RetroKtorGenerated] clients */
interface RetroKtorClientImpl
