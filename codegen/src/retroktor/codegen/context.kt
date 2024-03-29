package retroktor.codegen

import com.google.devtools.ksp.processing.JvmPlatformInfo
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import io.ktor.client.statement.*
import io.ktor.http.*


interface ProcessingContext : Resolver, KSPLogger {
  val resolver: Resolver
  val logger: KSPLogger
  val types: Types
  val typeNames: TypeNames

  val generateLazyCtors: Boolean
  val clientAnnotationSearchInDepth: Boolean

  val isJvm: Boolean
}

interface FunctionProcessingContext : ProcessingContext {
  val fn: KSFunctionDeclaration

  var method: String?
  var url: String?
  var urlParams: Set<String>
  var isMultipart: Boolean
  var isFormUrlEncoded: Boolean
  var allowsBody: Boolean

  var gotUrl: Boolean
  var gotPath: Boolean
  var gotBody: Boolean
  var gotPart: Boolean
  var gotField: Boolean
  var gotQuery: Boolean
  var gotQueryName: Boolean
  var gotQueryMap: Boolean

  val requestBlock: CodeBlock.Builder
  val urlStringBlock: CodeBlock.Builder
  val urlBlock: CodeBlock.Builder
  val headersBlock: CodeBlock.Builder
  val paramsBlock: CodeBlock.Builder
  val formBodyBlock: CodeBlock.Builder
}

interface AnnotationProcessingContext : FunctionProcessingContext {
  val ann: KSAnnotation
}

interface ParameterProcessingContext : FunctionProcessingContext {
  val param: KSValueParameter
  val paramType: KSType
  val paramTypeName: TypeName
}


context(SymbolProcessorEnvironment) class ProcessingContextImpl(
  override val resolver: Resolver,
  override val logger: KSPLogger,
) : ProcessingContext, Resolver by resolver, KSPLogger by logger {

  override val types = Types()

  override val typeNames = TypeNames()

  override val generateLazyCtors: Boolean
    get() = options["retroktor.client.lazyConstructors"]?.toBoolean() ?: true

  override val clientAnnotationSearchInDepth: Boolean
    get() = options["retroktor.client.searchLocalOrAnonymousClasses"]?.toBoolean() ?: false

  override val isJvm: Boolean
    get() = platforms.any { it is JvmPlatformInfo }
}

context(ProcessingContext) class FunctionProcessingContextImpl(
  override val fn: KSFunctionDeclaration,
) : FunctionProcessingContext, ProcessingContext by self {

  private val returns = fn.returnType!!.resolve().declaration.qualifiedName?.asString()
  private val returnsUnit = returns == "kotlin.Unit"
  private val returnsHttpResponse = returns == HttpResponse::class.qualifiedName

  override var method: String? = null
  override var url: String? = null
  override var urlParams: Set<String> = emptySet()
  override var isMultipart = false
  override var isFormUrlEncoded = false
  override var allowsBody = false

  override var gotUrl = false
  override var gotField = false
  override var gotPart = false
  override var gotBody = false
  override var gotPath = false
  override var gotQuery = false
  override var gotQueryName = false
  override var gotQueryMap = false

  override val urlStringBlock = CodeBlock.builder()
  override var urlBlock = CodeBlock.builder()
  override val headersBlock = CodeBlock.builder()
  override val paramsBlock = CodeBlock.builder()
  override val formBodyBlock = CodeBlock.builder()
  override val requestBlock = CodeBlock.builder()

  override fun error(message: String, symbol: KSNode?) = logger.error(message, symbol ?: fn)

  internal fun codeBlock() = buildCodeBlock {
    add("\n")
    if (!returnsUnit) add("return ")
    add("client.%M%L·{\n", method(), urlStringBlock())
    withIndent {
      addNonEmpty(urlBlock)
      addNonEmpty(headersBlock)
      addNonEmpty(paramsBlock)
      val blockFn = when {
        isFormUrlEncoded -> formUrlBody
        isMultipart -> multipartBody
        else -> null
      }
      if (blockFn != null && formBodyBlock.isNotEmpty()) {
        beginControlFlow("%M", blockFn)
        add(formBodyBlock.build())
        endControlFlow()
      }
      addNonEmpty(requestBlock)
    }
    add("}")
    if (!returnsHttpResponse && !returnsUnit) add(".%M()", bodyFn)
    add("\n")
  }

  private fun method() = methodFns[method] ?: run {
    // Unknown http method, first statement should specify method.
    //  Update url block to include method
    urlBlock = CodeBlock.builder()
      .addStatement("method = %T.parse(%S)", HttpMethod::class, method)
      .add(urlBlock.build())

    // Return default request
    defaultRequestFn
  }

  private fun urlStringBlock() = buildCodeBlock {
    if (hasUrl) urlStringBlock.add("\"%L\"", url)
    if (urlStringBlock.isNotEmpty()) add("(%L)", urlStringBlock.build())
  }
}

context(FunctionProcessingContext) class AnnotationProcessingContextImpl(
  override val ann: KSAnnotation,
) : AnnotationProcessingContext, FunctionProcessingContext by self {
  override fun error(message: String, symbol: KSNode?) = logger.error(message, symbol ?: ann)
}

context(FunctionProcessingContext) class ParameterProcessingContextImpl(
  override val param: KSValueParameter,
) : ParameterProcessingContext, FunctionProcessingContext by self {

  override val paramType by lazy {
    when {
      param.isVararg -> types.arrayType.replace(listOf(getTypeArgument(param.type, Variance.INVARIANT)))
      else -> param.type.resolve()
    }
  }
  override val paramTypeName by lazy { param.type.asTypeName() }

  override fun error(message: String, symbol: KSNode?) = logger.error(message, symbol ?: param)
}


private fun CodeBlock.Builder.addNonEmpty(builder: CodeBlock.Builder) = apply {
  if (builder.isNotEmpty()) add(builder.build())
}

// Necessary until being able to this@ reference context receivers is fixed
inline val ProcessingContext.self get() = this
inline val FunctionProcessingContext.self get() = this

internal inline val FunctionProcessingContext.hasMethod get() = method != null
internal inline val FunctionProcessingContext.hasUrl get() = url != null
internal inline val ParameterProcessingContext.paramName get() = param.name?.asString()
