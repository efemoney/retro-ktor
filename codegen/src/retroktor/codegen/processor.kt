package retroktor.codegen

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.*
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.ktor.client.*
import retroktor.RetroKtorClient
import retroktor.internal.RetroKtorClientImpl
import retroktor.internal.RetroKtorGenerated
import retroktor.internal.dsl.RetroKtorDsl

class RetroKtorProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment) = with(environment) { RetroKtorProcessor() }
}

context(SymbolProcessorEnvironment) class RetroKtorProcessor : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    with(ProcessingContextImpl(resolver, logger)) {
      getSymbolsWithAnnotation(RetroKtorClient::class.qualifiedName!!, inDepth = clientAnnotationSearchInDepth)
        .filter { it.isValidClientInterface() }
        .forEach { (it as KSClassDeclaration).generateFile(codeGenerator) }
    }
    return emptyList()
  }
}

context(ProcessingContext) private fun KSAnnotated.isValidClientInterface(): Boolean {

  if (this !is KSClassDeclaration || classKind != ClassKind.INTERFACE) {
    warn("Cannot annotate $this with @RetroKtorClient. Skipping as only interfaces are supported.", this)
    return false
  }

  val supersWithTypeParams = hierarchy.drop(1).filter { it.typeParameters.isNotEmpty() }.toList()
  val hasSupersWithTypeParams = supersWithTypeParams.isNotEmpty()
  val selfHasTypeParams = typeParameters.isNotEmpty()

  if (selfHasTypeParams || hasSupersWithTypeParams) {
    val thisStr = this.toString()
    val superStr = supersWithTypeParams.joinToString()
    error(buildString {
      append("Type parameters are unsupported on $thisStr")
      if (selfHasTypeParams && hasSupersWithTypeParams) append(" and its")
      if (hasSupersWithTypeParams) append(" superinterface(s): $superStr")
    }, this)
    return false
  }

  return true
}

context(ProcessingContext) private fun KSClassDeclaration.generateFile(generator: CodeGenerator) {
  FileSpec
    .builder(packageName.asString(), implName)
    .addAnnotation(fileSuppressAnnotation)
    .apply { if (generateLazyCtors) addFunction(implLambdaFunSpec()) }
    .addFunction(implFunSpec())
    .addType(implTypeSpec())
    .build()
    .writeTo(generator, aggregating = false, originatingKSFiles = setOf(containingFile!!))
}

context(ProcessingContext) private fun KSClassDeclaration.implFunSpec(): FunSpec {
  return FunSpec
    .builder(simpleName.asString())
    .addAnnotation(RetroKtorDsl::class)
    .addAnnotation(RetroKtorGenerated::class)
    .returns(toClassName())
    .addParameter(ParameterSpec("client", typeNames.client))
    .addCode("return %L { client }", implName)
    .addOriginatingKSFile(containingFile!!)
    .build()
}

context(ProcessingContext) private fun KSClassDeclaration.implLambdaFunSpec(): FunSpec {
  return FunSpec
    .builder(simpleName.asString())
    .addAnnotation(RetroKtorDsl::class)
    .addAnnotation(RetroKtorGenerated::class)
    .returns(toClassName())
    .addParameter(ParameterSpec("client", typeNames.lambda.noArgsToClient))
    .addCode("return %L(client)", implName)
    .addOriginatingKSFile(containingFile!!)
    .build()
}

context(ProcessingContext) private fun KSClassDeclaration.implTypeSpec(): TypeSpec {
  return TypeSpec.classBuilder(implName)
    .addAnnotation(RetroKtorGenerated::class)
    .addModifiers(PRIVATE)
    .addSuperinterface(toClassName())
    .addSuperinterface(RetroKtorClientImpl::class)
    .primaryConstructor(FunSpec.constructorBuilder().addParameter("client", typeNames.lambda.noArgsToClient).build())
    .addProperty(
      PropertySpec.builder("client", HttpClient::class, PRIVATE)
        .delegate("lazy(client)")
        .build()
    )
    .addFunctions(implFunctions())
    .addOriginatingKSFile(containingFile!!)
    .build()
}

context(ProcessingContext) private fun KSClassDeclaration.implFunctions(): Iterable<FunSpec> {
  return getAllFunctions()
    .filter { it.isAbstract }
    .onEach { if (Modifier.SUSPEND !in it.modifiers) error("Service method must be a suspend fun", it) }
    .map {
      FunSpec.builder(it.simpleName.asString())
        .addModifiers(PUBLIC, OVERRIDE, SUSPEND)
        .addParameters(it.parameters.toParameterSpecs())
        .returns(it.returnType!!.asTypeName())
        .addCode(FunctionProcessingContextImpl(it).apply { parseFunction() }.codeBlock())
        .build()
    }
    .asIterable()
}
