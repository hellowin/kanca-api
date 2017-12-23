package io.kanca.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import io.kanca.infra.MySQL
import io.kanca.repository.GroupFeedRepo

case class GroupRequest(groupId: String, token: String)

class CoreController extends Controller {

  get("/") { request: Request =>
    s"Server is ready"
  }

  get("/group-feeds") { req: Request =>
    val groupId = req.getParam("group_id")
    val page = if (req.getIntParam("page") > 0) req.getIntParam("page") else 1

    GroupFeedRepo.read(MySQL.getConnection, page)
  }

}
