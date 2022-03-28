package retroktor.internal

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.DataConversion
import io.ktor.util.converters.*
import io.ktor.util.reflect.*
import retroktor.internal.ToStringConversion.toString

inline fun <reified T : Any> HttpClient.converted(value: T): List<String> {
  val conversion = pluginOrNull(DataConversion) ?: DefaultConversionService
  return conversion.toValues(value)
}

/** [ConversionService] that only converts [toString] but not back, in most cases */
object ToStringConversion : ConversionService {

  override fun fromValues(values: List<String>, type: TypeInfo): Any? {
    return DefaultConversionService.fromValues(values, type)
  }

  override fun toValues(value: Any?): List<String> {
    return DefaultConversionService.toValues (value)
  }
}