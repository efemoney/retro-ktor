package retroktor

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import retroktor.http.*

suspend fun main() {
  val client = HttpClient(OkHttp) {
    defaultRequest { url("https://api.github.com") }
  }
  val github = GithubClient(client)
  println(github.user("caareem"))
}

@RetroKtorClient
interface GithubClient : SuperGithubClient {

  @GET("/users/{username}?")
  @Headers("Accept: application/json")
  suspend fun user(@Path("username") username: String): HttpResponse

  @GET("/repos/efemoney/lexiko/branches")
  override suspend fun lexikoBranches(): HttpResponse

  @POST("/haha/{lol}")
  suspend fun efemoney(
    @Path("lol") lolValue: String,
    @QueryMap filters: Map<String, List<String>>,
  )
}

interface SuperGithubClient {

  @GET("/repos/{owner}/{repo}/branches")
  suspend fun branches(
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Query("filters") vararg filters: String,
  ): HttpResponse

  @GET("/emojis")
  suspend fun emojis(
    @Query("filters") filters: List<String>,
    @QueryName vararg names: String,
  )

  suspend fun lexikoBranches() = branches("efemoney", "lexiko")
}
