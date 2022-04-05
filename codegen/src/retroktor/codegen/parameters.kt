@file:Suppress("FunctionName")

package retroktor.codegen

import com.squareup.kotlinpoet.*
import io.ktor.http.*
import retroktor.http.*
import retroktor.http.Url

private val UrlTypeName = io.ktor.http.Url::class.asTypeName()
private val UrlBuilderTypeName = URLBuilder::class.asClassName()
private val UrlBuilderReceiverLambdaTypeName = // URLBuilder.() -> Unit
  LambdaTypeName.get(
    receiver = UrlBuilderTypeName,
    returnType = UNIT
  )
private val UrlBuilderReceiverAndParamLambdaTypeName = // URLBuilder.(URLBuilder) -> Unit
  LambdaTypeName.get(
    receiver = UrlBuilderTypeName,
    returnType = UNIT,
    parameters = listOf(ParameterSpec.unnamed(UrlBuilderTypeName)),
  )

fun ParameterProcessingContext.parseParameter() {
  param.annotations.withEach {
    ifMatches<Url> { parseUrl() }
    ifMatches<Path> { parsePath(it) }
    ifMatches<Query> { parseQuery(it) }
    ifMatches<QueryMap> { parseQueryMap(it) }
    ifMatches<QueryName> { parseQueryName(it) }
    ifMatches<Header> { parseHeader(it) }
    ifMatches<HeaderMap> { parseHeaderMap() }
  }
}

context(ParameterProcessingContext) fun parseUrl() {

  if (gotUrl) return error("Multiple @Url method annotations found.")
  if (gotPath) return error("@Path parameters may not be used with @Url.")
  if (gotQuery) return error("A @Url parameter must not come after a @Query.")
  if (gotQueryName) return error("A @Url parameter must not come after a @QueryName.")
  if (gotQueryMap) return error("A @Url parameter must not come after a @QueryMap.")
  if (hasUrl) return error("@Url cannot be used with @$method URL")

  gotUrl = true

  when {
    paramType == types.stringType
    -> urlStringBlock.add("%L", paramName)

    // Using type name checks here because lambdas resolve to FunctionN<...>
    //  and figuring receivers, parameters & return type of those is a hassle
    paramTypeName == UrlBuilderReceiverAndParamLambdaTypeName
      || paramTypeName == UrlBuilderReceiverLambdaTypeName
      || paramTypeName == UrlTypeName
      || (isJvm && paramType == types.URLType)
    -> urlBlock.addStatement("%M(%L)", urlFn, paramName)

    else
    -> error("@Url must be String, Url, URLBuilder.() -> Unit or URLBuilder.(URLBuilder) -> Unit type.")
  }
}

context(ParameterProcessingContext) fun parsePath(path: Path) {
  if (gotQuery) return error("A @Path parameter must not come after a @Query.")
  if (gotQueryName) return error("A @Path parameter must not come after a @QueryName.")
  if (gotQueryMap) return error("A @Path parameter must not come after a @QueryMap.")
  if (gotUrl) return error("@Path parameters may not be used with @Url.")

  val name = path.value

  if (!hasUrl) return error("@Path can only be used with relative url on @$method")
  if (paramType.isMarkedNullable) return error("Path parameter '$name' must not be nullable.")

  gotPath = true

  if (!PARAM_NAME_REGEX.matches(name))
    return error("@Path parameter name must match $PARAM_NAME_REGEX. Found: $name")

  if (name !in urlParams)
    return error("URL '$url' does not contain {$name}.")

  val newUrl = when (paramType) {
    types.stringType -> url!!.replace("{$name}", CodeBlock.of("%L", "\$$paramName").toString())
    else -> return error("@Path parameter must be String type. $PleaseFileIssue")
  }

  url = newUrl
  urlParams = urlParams - name
}

context(ParameterProcessingContext) fun parseQuery(query: Query) {
  gotQuery = true

  val name = query.value
  val parameterFn = if (query.encoded) encodedParameter else parameter
  val parametersFn = if (query.encoded) encodedParameters else parameters

  when {
    paramType == types.stringType
    -> paramsBlock.addStatement("%M(%S, %L)", parameterFn, name, paramName)

    types.iterableType.isAssignableFrom(paramType)
      || types.arrayType.isAssignableFrom(paramType)
    -> paramsBlock.addStatement("%M(%S, %L)", parametersFn, name, paramName)

    else -> return error(
      "Unsupported @Query type: $paramType. Should be String, List<String> or Array<String>(including varargs)"
    )
  }
}

context(ParameterProcessingContext) fun parseQueryMap(query: QueryMap) {
  gotQueryMap = true

  val parametersFn = if (query.encoded) encodedParameters else parameters

  when {
    types.mapType.isAssignableFrom(paramType) -> {
      val (keyType, valueType) = paramType.arguments.map { it.type!!.resolve() }
      if (keyType != types.stringType) return error("@QueryMap key type must be String.")

      when {
        valueType == types.stringType
          || types.arrayType.isAssignableFrom(valueType)
          || types.iterableType.isAssignableFrom(valueType)
        -> paramsBlock.addStatement("%M(%L)", parametersFn, paramName)

        else -> return error("@QueryMap value type must be String, List<String> or Array<String>(including varargs)")
      }
    }
    else -> return error("Unsupported @QueryMap type: $paramType")
  }
}

context(ParameterProcessingContext) fun parseQueryName(query: QueryName) {
  gotQueryName = true

  val parameterFn = if (query.encoded) encodedParameterName else parameterName
  val parametersFn = if (query.encoded) encodedParameterNames else parameterNames

  when {

    paramType == types.stringType
    -> paramsBlock.addStatement("%M(%L)", parameterFn, paramName)

    types.iterableType.isAssignableFrom(paramType)
      || types.arrayType.isAssignableFrom(paramType)
    -> paramsBlock.addStatement("%M(%L)", parametersFn, paramName)

    else
    -> return error("Unsupported @QueryName type: $paramType. Should be String, List<String> or Array<String>")
  }
}

context(ParameterProcessingContext) fun parseHeader(header: Header) {
  val name = header.value

  when {
    paramType == types.stringType
    -> headersBlock.addStatement("%M(%S, $paramName)", header, name)

    types.listType.isAssignableFrom(paramType)
    -> headersBlock.addStatement("%M(%S, $paramName)", headers, name)

    types.arrayType.isAssignableFrom(paramType)
    -> headersBlock.addStatement("%M(%S, $paramName)", headers, name)

    else -> return error("Unsupported @Header type: $paramType. $PleaseFileIssue")
  }
}

context(ParameterProcessingContext) fun parseHeaderMap() {

  when {
    types.mapType.isAssignableFrom(paramType) -> {
      val (keyType, valueType) = paramType.arguments.map { it.type!!.resolve() }
      if (keyType != types.stringType) return error("@HeaderMap key type must be String.")

      when {
        valueType == types.stringType
          || types.listType.isAssignableFrom(valueType)
          || types.arrayType.isAssignableFrom(valueType)
        -> headersBlock.addStatement("%M($paramName)", headers)
      }
    }
    else -> return error("Unsupported @QueryMap type: $paramType")
  }
}
