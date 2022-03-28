package retroktor.codegen

import com.squareup.kotlinpoet.MemberName

val urlFn = MemberName("io.ktor.client.request", "url")
val bodyFn = MemberName("io.ktor.client.call", "body")
val headerFn = MemberName("io.ktor.client.request", "header")
val defaultRequestFn = MemberName("io.ktor.client.request", "request")
val requestFns = mapOf(
  "GET" to MemberName("io.ktor.client.request", "get"),
  "POST" to MemberName("io.ktor.client.request", "post"),
  "PUT" to MemberName("io.ktor.client.request", "put"),
  "PATCH" to MemberName("io.ktor.client.request", "patch"),
  "HEAD" to MemberName("io.ktor.client.request", "head"),
  "OPTIONS" to MemberName("io.ktor.client.request", "options"),
  "DELETE" to MemberName("io.ktor.client.request", "delete"),
)
val valuesOfFn = MemberName("io.ktor.util", "valuesOf")
val parametersOfFn = MemberName("io.ktor.http", "parametersOf")


val emptyList = MemberName("kotlin.collections", "emptyList")
val toListFn = MemberName("kotlin.collections", "toList")
val associateWithFn = MemberName("kotlin.collections", "associateWith")


val appendMap = MemberName("retroktor.internal", "appendMap")
val appendMapList = MemberName("retroktor.internal", "appendMapList")
val appendMapArray = MemberName("retroktor.internal", "appendMapArray")
val appendName = MemberName("retroktor.internal", "appendName")
val appendNames = MemberName("retroktor.internal", "appendNames")
