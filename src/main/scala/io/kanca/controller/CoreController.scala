package io.kanca.controller

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import io.kanca.core.Repository

class CoreController @Inject()(repo: Repository) extends Controller {

  get("/") { request: Request =>
    s"Server is ready"
  }

  get("/group_feed/:group_id") { req: Request =>
    val groupId = req.getParam("group_id")
    val page = req.getIntParam("page", 1)
    val limit = req.getIntParam("limit", 100)
    repo.readGroupFeed(groupId, page, limit)
  }

}
