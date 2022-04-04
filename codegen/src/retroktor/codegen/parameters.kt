@file:Suppress("FunctionName")

package retroktor.codegen

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.*
import io.ktor.http.*
import retroktor.http.*
import retroktor.http.Url
import java.net.URL

private val URLTypeName = URL::class.asTypeName()
private val UrlTypeName = io.ktor.http.Url::class.asTypeName()
private val UrlBuilderTypeName = URLBuilder::class.asClassName()
private val UrlBuilderContextLambdaTypeName = // URLBuilder.() -> Unit
  LambdaTypeName.get(
    receiver = UrlBuilderTypeName,
    returnType = UNIT
  )
private val UrlBuilderContextAndParamLambdaType = // URLBuilder.(URLBuilder) -> Unit
  LambdaTypeName.get(
    receiver = UrlBuilderTypeName,
    returnType = UNIT,
    parameters = listOf(ParameterSpec.unnamed(UrlBuilderTypeName)),
  )

context(Resolver) private val MapType
  get() = getClassDeclarationByName("kotlin.collections.Map")!!.asStarProjectedType()

context(Resolver) private val ListType
  get() = getClassDeclarationByName("kotlin.collections.List")!!.asStarProjectedType()

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

  when (paramTypeName) {
    STRING
    -> urlStringBlock.add("%L", paramName)

    UrlBuilderContextAndParamLambdaType,
    UrlBuilderContextLambdaTypeName,
    UrlTypeName,
    URLTypeName,
    -> requestBlock.addStatement("%M(%L)", urlFn, paramName)

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
  if (paramTypeName.isNullable) return error("Path parameter '$name' must not be nullable.")

  gotPath = true

  if (!PARAM_NAME_REGEX.matches(name))
    return error("@Path parameter name must match $PARAM_NAME_REGEX. Found: $name")

  if (name !in urlParams)
    return error("URL '$url' does not contain {$name}.")

  val newUrl = when (paramTypeName) {
    STRING -> url!!.replace("{$name}", CodeBlock.of("%L", "\$$paramName").toString())
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
    paramType == builtIns.stringType
    -> paramsBlock.addStatement("%M(%S, %L)", parameterFn, name, paramName)

    builtIns.iterableType.isAssignableFrom(paramType)
    -> paramsBlock.addStatement("%M(%S, %L)", parametersFn, name, paramName)

    builtIns.arrayType.isAssignableFrom(paramType)
    -> paramsBlock.addStatement("%M(%S, %L.%M())", parametersFn, name, paramName, toListFn)

    else -> return error(
      "Unsupported @Query type: $paramType. Should be String, List<String> or Array<String>(including varargs)"
    )
  }
}

context(ParameterProcessingContext) fun parseQueryMap(query: QueryMap) {
  gotQueryMap = true

  val parametersFn = if (query.encoded) encodedParameters else parameters

  when {
    MapType.isAssignableFrom(paramType) -> {
      val (keyType, valueType) = paramType.arguments.map { it.type!!.resolve() }
      if (keyType != builtIns.stringType) return error("@QueryMap key type must be String.")

      when {
        valueType == builtIns.stringType
          || builtIns.arrayType.isAssignableFrom(valueType)
          || builtIns.iterableType.isAssignableFrom(valueType)
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

    paramType == builtIns.stringType
    -> paramsBlock.addStatement("%M(%L)", parameterFn, paramName)

    builtIns.iterableType.isAssignableFrom(paramType)
    -> paramsBlock.addStatement("%M(%L)", parametersFn, paramName)

    builtIns.arrayType.isAssignableFrom(paramType)
    -> paramsBlock.addStatement("%M(%L)", parametersFn, paramName)

    else
    -> return error("Unsupported @QueryName type: $paramType. Should be String, List<String> or Array<String>")
  }
}

context(ParameterProcessingContext) fun parseHeader(header: Header) {
  val name = header.value

  when {
    paramType == builtIns.stringType
    -> headersBlock.addStatement("%M(%S, $paramName)", header, name)

    ListType.isAssignableFrom(paramType)
    -> headersBlock.addStatement("%M(%S, $paramName)", headers, name)

    builtIns.arrayType.isAssignableFrom(paramType)
    -> headersBlock.addStatement("%M(%S, $paramName)", headers, name)

    else -> return error("Unsupported @Header type: $paramType. $PleaseFileIssue")
  }
}

context(ParameterProcessingContext) fun parseHeaderMap() {

  when {
    MapType.isAssignableFrom(paramType) -> {
      val (keyType, valueType) = paramType.arguments.map { it.type!!.resolve() }
      if (keyType != builtIns.stringType) return error("@HeaderMap key type must be String.")

      when {
        builtIns.arrayType.isAssignableFrom(valueType)
        -> headersBlock.addStatement("%M($paramName)", headers)

        ListType.isAssignableFrom(valueType)
        -> headersBlock.addStatement("%M($paramName)", headers)

        valueType == builtIns.stringType
        -> headersBlock.addStatement("%M($paramName)", headers)
      }
    }
    else -> return error("Unsupported @QueryMap type: $paramType")
  }
}
