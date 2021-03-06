package io.kanca

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import io.kanca.controller.CoreController
import io.kanca.fbgraph.FBGraphModule
import io.kanca.repository.RepoModuleMySQL

object KancaApiServerMain extends KancaApiServer

class KancaApiServer extends HttpServer {

  override val modules = Seq(FBGraphModule, RepoModuleMySQL)

  override val defaultFinatraHttpPort: String = ":8080"

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[CoreController]
  }

}
