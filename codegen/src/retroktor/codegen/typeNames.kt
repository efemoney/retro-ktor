@file:Suppress("PropertyName")

package retroktor.codegen

import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.asTypeName
import io.ktor.client.*

context(ProcessingContext) class TypeNames {
  val client = HttpClient::class.asTypeName()
  val lambda = LambdaTypeNames()
}

context(ProcessingContext) class LambdaTypeNames {
  val noArgsToClient = LambdaTypeName.get(returnType = HttpClient::class.asTypeName())
}
