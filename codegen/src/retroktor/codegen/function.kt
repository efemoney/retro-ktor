package retroktor.codegen

import com.google.devtools.ksp.symbol.KSAnnotation
import retroktor.http.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/* Upper and lower characters, digits, underscores, and hyphens, starting with a character. */
// language=RegExp
internal const val PARAM = "[a-zA-Z][a-zA-Z0-9_-]*"
internal val PARAM_URL_REGEX = "\\{($PARAM)}".toRegex()
internal val PARAM_NAME_REGEX = PARAM.toRegex()

fun FunctionProcessingContext.parseFunction() {
  parseFunctionAnnotations()
  parseFunctionParameters()
}

context(FunctionProcessingContext) fun parseFunctionAnnotations() {
  fn.annotations.withEach {
    ifMatches<HEAD> { parseMethodAndPath("HEAD", it.value, false) }
    ifMatches<OPTIONS> { parseMethodAndPath("OPTIONS", it.value, false) }
    ifMatches<GET> { parseMethodAndPath("GET", it.value, false) }
    ifMatches<POST> { parseMethodAndPath("POST", it.value, true) }
    ifMatches<PUT> { parseMethodAndPath("PUT", it.value, true) }
    ifMatches<PATCH> { parseMethodAndPath("PATCH", it.value, true) }
    ifMatches<DELETE> { parseMethodAndPath("DELETE", it.value, false) }
    ifMatches<HTTP> { parseMethodAndPath(it.method.uppercase(), it.path, it.hasBody) }
    ifMatches<Headers> { parseHeaders(it.value) }
    ifMatches<Multipart> {
      if (isFormUrlEncoded) return error("Only one encoding annotation is allowed.")
      isMultipart = true
    }
    ifMatches<FormUrlEncoded> {
      if (isMultipart) return error("Only one encoding annotation is allowed.")
      isFormUrlEncoded = true
    }
  }

  if (!hasMethod) {
    return error("HTTP method annotation is required (e.g., @GET, @POST, etc.).")
  }

  if (!allowsBody) {
    if (isMultipart)
      return error("Multipart can only be specified on HTTP methods with request body (e.g., @POST).")
    if (isFormUrlEncoded)
      return error("FormUrlEncoded can only be specified on HTTP methods with request body (e.g., @POST).")
  }
}

context(FunctionProcessingContext) fun parseFunctionParameters() {
  fn.parameters.forEach {
    ParameterProcessingContextImpl(it).apply { parseParameter() }
  }

  if (urlParams.isNotEmpty()) {
    return error("Missing @Path parameters: ${urlParams.joinToString()}")
  }
}

context(AnnotationProcessingContext) fun parseMethodAndPath(method: String, value: String, hasBody: Boolean) {
  if (hasMethod) return error("Only one HTTP method is allowed. Found: $method and ${self.method}.")

  self.method = method
  self.allowsBody = hasBody

  if (value.isEmpty()) return

  // Ensure the query string does not have any named parameters.
  value.substringAfter('?', missingDelimiterValue = "").let {
    if (PARAM_URL_REGEX in it)
      return error("URL query string ?$it must not have parameters. For dynamic query parameters use @Query.")
  }

  urlParams = value.parsePathParams()
  url = value
}

context(AnnotationProcessingContext) fun parseHeaders(headers: Array<out String>) {
  headers
    .ifEmpty { return error("@Headers annotation is empty.") }
    .forEach { line ->
      if (':' !in line) return error("@Headers value must be in the form 'Name: Value'. Found: '$line'")
      appendHeaderLine(line)
    }
}

context(FunctionProcessingContext)
  internal inline fun <reified T : Annotation> KSAnnotation.ifMatches(
  then: context(AnnotationProcessingContext) (T) -> Unit
) {
  contract {
    callsInPlace(then, InvocationKind.AT_MOST_ONCE)
  }
  if (matches<T>()) then(AnnotationProcessingContextImpl(this), reify())
}

private fun String.parsePathParams() = PARAM_URL_REGEX.findAll(this).map { it.groupValues[1] }.toSet()
