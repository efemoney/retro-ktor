@file:Suppress("UNCHECKED_CAST")

package retroktor.codegen

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.concurrent.ConcurrentHashMap
import kotlin.Array
import kotlin.Enum
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.jvmErasure
import java.lang.reflect.Array as ReflectArray

internal const val PleaseFileIssue =
  "\n    Please file an issue at github.com/efemoney/retro-ktor/issues to improve support."

private val reificationCache = ConcurrentHashMap<KClass<*>, ReificationFunction<*>>(8)

private class ReificationFunction<T : Annotation>(annotationKClass: KClass<T>) : (KSAnnotation) -> T {
  val ctor by lazy { annotationKClass.constructors.single() }
  val params by lazy { ctor.parameters.associateBy { it.name!! } }

  override fun invoke(annotation: KSAnnotation): T {
    val actualArgs = buildMap {
      annotation.arguments.forEach { arg ->
        if (arg.isSpread) error("Unsupported spread annotation argument $arg. $PleaseFileIssue")
        val name = arg.name?.asString()!!
        val parameter = params[name]!!
        put(parameter, actualValue(arg.value, parameter.type.jvmErasure))
      }
    }

    return ctor.callBy(actualArgs)
  }

  // Todo: possibly cache actualValue computation lambdas?
  private fun actualValue(value: Any?, parameterKls: KClass<*>): Any? = when (value) {
    is KSAnnotation -> value.reify(parameterKls as KClass<Annotation>)
    is KSType -> {
      val declaration = value.declaration
      when {
        parameterKls.isSubclassOf(Enum::class) -> {
          require(declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_ENTRY)
          parameterKls.staticFunctions
            .first { it.name == "valueOf" }
            .call(declaration.simpleName.asString())
        }
        parameterKls.isSubclassOf(KClass::class) -> {
          Class.forName(declaration.qualifiedName!!.asString()).kotlin
        }
        else -> error("Unsupported annotation value $value. $PleaseFileIssue")
      }
    }
    is List<*> -> {
      require(parameterKls.java.isArray)
      value.toArray(parameterKls)
    }
    else -> value
  }

  private fun List<Any?>.toArray(kls: KClass<*>): Any = when (kls.qualifiedName) {
    "kotlin.BooleanArray" -> (this as List<Boolean>).toBooleanArray()
    "kotlin.ByteArray" -> (this as List<Byte>).toByteArray()
    "kotlin.ShortArray" -> (this as List<Short>).toShortArray()
    "kotlin.CharArray" -> (this as List<Char>).toCharArray()
    "kotlin.IntArray" -> (this as List<Int>).toIntArray()
    "kotlin.LongArray" -> (this as List<Long>).toLongArray()
    "kotlin.FloatArray" -> (this as List<Float>).toFloatArray()
    "kotlin.DoubleArray" -> (this as List<Double>).toDoubleArray()
    else -> {
      val componentCls = kls.java.componentType
      val componentKls = kls.java.componentType.kotlin
      val arr = ReflectArray.newInstance(componentCls, size) as Array<Any?>
      for (i in indices) arr[i] = actualValue(get(i), componentKls)
      arr
    }
  }
}

internal val fileSuppressAnnotation = AnnotationSpec.builder(Suppress::class)
  .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
  .addMember("%S, %S, %S", "RedundantVisibilityModifier", "RedundantUnitReturnType", "unused")
  .build()

internal val KSClassDeclaration.implName get() = simpleName.asString() + "Impl"

internal val KSClassDeclaration.hierarchy: Sequence<KSClassDeclaration>
  get() = sequenceOf(this) + superTypes.map { it.resolve().declaration }.flatMap {
    when (it) {
      is KSClassDeclaration -> it.hierarchy
      is KSTypeAlias -> it.findActualType().hierarchy
      else -> error("Unsupported super interface declaration $it with type ${it::class.simpleName}. $PleaseFileIssue")
    }
  }.distinct()

context(ProcessingContext) internal val KSFunctionDeclaration.parametersAsSpecs
  get() = parameters.map {
    val name = it.name!!.asString()
    if (it.isVararg && builtIns.arrayType.isAssignableFrom(it.type.resolve())) {
      ParameterSpec(name, it.type.resolve().arguments.single().type!!.asTypeName(), KModifier.VARARG)
    } else {
      ParameterSpec(name, it.type.asTypeName())
    }
  }

fun KSTypeReference.asTypeName() = when (val element = element) {
  is KSCallableReference -> element.asTypeName()
  else -> resolve().toTypeName()
}

fun KSCallableReference.asTypeName(): TypeName = LambdaTypeName.get(
  receiver = receiverType?.asTypeName(),
  returnType = returnType.asTypeName(),
  parameters = functionParameters.map { ParameterSpec.unnamed(it.type.asTypeName()) },
)

/** Like [Sequence.forEach] except with the item in scope of [action] */
internal inline fun <T> Sequence<T>.withEach(action: T.() -> Unit) {
  for (element in this) element.action()
}


internal inline fun <reified T : Annotation> KSAnnotation.matches() = matches(T::class)

internal fun <T : Annotation> KSAnnotation.matches(annotationKls: KClass<T>) =
  shortName.asString() == annotationKls.simpleName &&
    annotationType.resolve().declaration.qualifiedName?.asString() == annotationKls.qualifiedName

internal inline fun <reified T : Annotation> KSAnnotation.reify(): T = reify(T::class)

internal fun <T : Annotation> KSAnnotation.reify(annotationKClass: KClass<T>): T =
  reificationCache.getOrPut(annotationKClass) { ReificationFunction(annotationKClass) }.invoke(this) as T
