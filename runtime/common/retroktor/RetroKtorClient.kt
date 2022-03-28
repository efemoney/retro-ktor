package retroktor

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

/** Annotates a RetroKtor client interface. */
@MustBeDocumented
@Target(CLASS)
@Retention(RUNTIME)
annotation class RetroKtorClient
