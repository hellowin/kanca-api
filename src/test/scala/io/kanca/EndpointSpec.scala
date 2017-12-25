package io.kanca

import com.twitter.finagle.http.Response
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

class EndpointSpec extends FeatureTest {

  override val server = new EmbeddedHttpServer(new KancaApiServer)

  test("test root") {
    val response: Response = server.httpGet("/")
    response.contentString shouldEqual "Server is ready"
  }

}
