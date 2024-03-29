package retroktor

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import retroktor.http.*
import retroktor.http.Headers
import retroktor.http.Url

suspend fun main() {
  val client = HttpClient(OkHttp) {
    defaultRequest { url("https://api.github.com") }
    Logging { level = LogLevel.ALL }
    install(DefaultRequest) {}
    install(ContentNegotiation) {}
  }
  val github = GithubClient(client)
  println(github.user { })
  println(github.lexikoBranches())
}

@RetroKtorClient
interface GithubClient : SuperGithubClient {

  @GET
  @Headers("Accept: application/json")
  suspend fun user(@Url url: URLBuilder.() -> Unit): HttpResponse

  @POST("/haha")
  suspend fun efemoney(@HeaderMap headers: Map<String, String>)
}

interface SuperGithubClient {

  @GET("/repos/{owner}/{repo}/branches")
  suspend fun branches(
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Query("filters") vararg filters: String,
  ): HttpResponse

  @HTTP(method = "DELETE", path = "/emojis")
  suspend fun emojis(
    @Query("filters") filters: List<String>,
    @Header("X-User-ID") vararg userIds: String,
  )

  suspend fun lexikoBranches() = branches("efemoney", "lexiko")
}
