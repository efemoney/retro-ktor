package retroktor.codegen

import com.squareup.kotlinpoet.MemberName

val urlFn = MemberName("io.ktor.client.request", "url")
val bodyFn = MemberName("io.ktor.client.call", "body")
val defaultRequestFn = MemberName("io.ktor.client.request", "request")
val methodFns = mapOf(
  "GET" to MemberName("io.ktor.client.request", "get"),
  "POST" to MemberName("io.ktor.client.request", "post"),
  "PUT" to MemberName("io.ktor.client.request", "put"),
  "PATCH" to MemberName("io.ktor.client.request", "patch"),
  "HEAD" to MemberName("io.ktor.client.request", "head"),
  "OPTIONS" to MemberName("io.ktor.client.request", "options"),
  "DELETE" to MemberName("io.ktor.client.request", "delete"),
)

val formUrlBody = MemberName("retroktor.internal.dsl", "formUrlBody")
val multipartBody = MemberName("retroktor.internal.dsl", "multipartBody")
val header = MemberName("retroktor.internal.dsl", "header")
val headers = MemberName("retroktor.internal.dsl", "headers")
val parameter = MemberName("retroktor.internal.dsl", "parameter")
val parameters = MemberName("retroktor.internal.dsl", "parameters")
val parameterName = MemberName("retroktor.internal.dsl", "parameterName")
val parameterNames = MemberName("retroktor.internal.dsl", "parameterNames")
val encodedParameter = MemberName("retroktor.internal.dsl", "encodedParameter")
val encodedParameters = MemberName("retroktor.internal.dsl", "encodedParameters")
val encodedParameterName = MemberName("retroktor.internal.dsl", "encodedParameterName")
val encodedParameterNames = MemberName("retroktor.internal.dsl", "encodedParameterNames")
