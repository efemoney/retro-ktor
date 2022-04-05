@file:Suppress("PropertyName")

package retroktor.codegen

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import java.net.URL

context(Resolver) class Types : KSBuiltIns by builtIns {
  val mapType by lazy { getClassDeclarationByName("kotlin.collections.Map")!!.asStarProjectedType() }
  val listType by lazy { getClassDeclarationByName("kotlin.collections.List")!!.asStarProjectedType() }

  // Must precede with a JVM platform check
  val URLType by lazy { getClassDeclarationByName<URL>()!!.asStarProjectedType() }
}
