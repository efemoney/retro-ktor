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
  get() = getClassDeclarationByName<Map<String, *>>()?.asStarProjectedType()!!

context(ParameterProcessingContext) fun parseParameter() {
  param.annotations.withEach {
    ifMatches<Url> { parseUrl() }
    ifMatches<Path> { parsePath(it) }
    ifMatches<Query> { parseQuery(it) }
    ifMatches<QueryMap> { parseQueryMap(it) }
    ifMatches<QueryName> { parseQueryName(it) }
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

  when (paramType) {
    STRING
    -> urlStringBlock.add("%L", paramName)

    UrlBuilderContextAndParamLambdaType,
    UrlBuilderContextLambdaTypeName,
    URLTypeName, UrlTypeName,
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
  if (paramType.isNullable) return error("Path parameter '$name' must not be null.")

  gotPath = true

  if (!PARAM_NAME_REGEX.matches(name))
    return error("@Path parameter name must match $PARAM_NAME_REGEX. Found: $name")

  if (name !in urlParams)
    return error("URL '$url' does not contain {$name}.")

  val newUrl = when (paramType) {
    STRING -> url!!.replace("{$name}", CodeBlock.of("%L", "\$$paramName").toString())
    else -> return error("@Path parameter must be String type. $PleaseFileIssue")
  }

  url = newUrl
  urlParams = urlParams - name
}

context(ParameterProcessingContext) fun parseQuery(query: Query) {
  gotQuery = true

  val name = query.value
  val parameterType = param.type.resolve()
  val parameters = if (query.encoded) "encodedParameters" else "parameters"

  when {

    parameterType == builtIns.stringType
    -> urlBlock.addStatement("$parameters.append(%S, $paramName)", name)

    builtIns.iterableType.isAssignableFrom(parameterType)
    -> urlBlock.addStatement("$parameters.appendAll(%S, $paramName)", name)

    builtIns.arrayType.isAssignableFrom(parameterType)
    -> urlBlock.addStatement("$parameters.appendAll(%S, $paramName.%M())", name, toListFn)

    else
    -> return error("Unsupported @Query type: $parameterType. Should be String, List<String> or Array<String>")
  }
}

context(ParameterProcessingContext) fun parseQueryMap(query: QueryMap) {
  gotQueryMap = true

  val parameterType = param.type.resolve()
  val parameters = if (query.encoded) "encodedParameters" else "parameters"

  when {
    MapType.isAssignableFrom(parameterType) -> {
      val (keyType, valueType) = parameterType.arguments.map { it.type!!.resolve() }
      if (keyType != builtIns.stringType) return error("@QueryMap map key type must be String.")

      when {
        valueType == builtIns.stringType
        -> urlBlock.addStatement("$parameters.%M($paramName)", appendMap)

        builtIns.iterableType.isAssignableFrom(valueType)
        -> urlBlock.addStatement("$parameters.%M($paramName)", appendMapList)

        builtIns.arrayType.isAssignableFrom(valueType)
        -> urlBlock.addStatement("$parameters.%M($paramName)", appendMapArray)
      }
    }
    else -> return error("Unsupported @QueryMap type: $parameterType")
  }
}

context(ParameterProcessingContext) fun parseQueryName(query: QueryName) {
  gotQueryName = true

  val parameterType = param.type.resolve()
  val parameters = if (query.encoded) "encodedParameters" else "parameters"

  when {

    parameterType == builtIns.stringType
    -> urlBlock.addStatement("$parameters.%M($paramName)", appendName)

    builtIns.iterableType.isAssignableFrom(parameterType)
    -> urlBlock.addStatement("$parameters.%M($paramName)", appendNames)

    builtIns.arrayType.isAssignableFrom(parameterType)
    -> urlBlock.addStatement("$parameters.%M(*$paramName)", appendNames)

    else
    -> return error("Unsupported @QueryName type: $parameterType. Should be String, List<String> or Array<String>")
  }
}
