package io.kanca.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class CoreController extends Controller {

  get("/") { request: Request =>
    s"Server is ready"
  }

}
