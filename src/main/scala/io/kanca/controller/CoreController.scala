package io.kanca.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import io.kanca.fbgraph.Graph

case class GroupRequest(groupId: String, token: String)

class CoreController extends Controller {

  get("/") { request: Request =>
    s"Server is ready"
  }

  get("/group-feeds") { req: Request =>
    Graph.getGroupFeeds(req.getParam("token"), req.getParam("group_id"))
  }

}
