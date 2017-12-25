package io.kanca.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import io.kanca.repository.{GroupFeedMySQL, RepositoryMySQL}

case class GroupRequest(groupId: String, token: String)

class CoreController extends Controller {

  get("/") { request: Request =>
    s"Server is ready"
  }

}
