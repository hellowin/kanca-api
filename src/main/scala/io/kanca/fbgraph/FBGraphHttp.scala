package io.kanca.fbgraph

import play.api.libs.json._

import scalaj.http._

class FBGraphHttp(
  version: String,
  defaultPageLimit: Int,
  defaultRequestLimit: String,
) extends FBGraph(defaultPageLimit) {

  private val FB_URL: String = s"https://graph.facebook.com/v$version"

  debug(s"Initiated FB Graph API with base URL: $FB_URL")

  private def getListResult[T](req: HttpRequest, token: String, parser: JsObject => T, pageLimit: Int, results: List[T] = List()): List[T] = {
    val resString: String = req.asString.body
    val (result, next) = parseStringResultArray[T](resString, parser)

    val newResults: List[T] = results ::: result

    if (next.orNull == null || pageLimit - 1 <= 0) return newResults

    // rebuild request if request is post, token and method params are vanished
    val nextReq: HttpRequest = getHttpRequest(token, next.get)

    getListResult[T](nextReq, token, parser, pageLimit - 1, newResults)
  }

  private def getHttpRequest(token: String, node: String, fields: String = null, limit: String = defaultRequestLimit): HttpRequest = {
    var params = Seq(
      "method" -> "GET",
      "limit" -> limit.toString,
      "access_token" -> token
    )
    var url = s"$FB_URL/$node"
    if (fields != null) {
      params = params :+ ("fields" -> fields)
    } else {
      url = node
    }
    Http(url).params(params).postForm
  }

  @throws(classOf[Exception])
  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = defaultPageLimit): List[GroupFeed] = {
    val req: HttpRequest = getHttpRequest(
      token, s"$groupId/feed",
      "created_time,id,message,updated_time,caption,story,description,from,link,name,picture,status_type,type,shares,permalink_url,to,message_tags"
    )

    debug(s"get group feeds url = ${req.url} params = ${req.params.map { case (key: String, value: String) => s"$key = $value" }}")

    getListResult[GroupFeed](req, token, GroupFeed.parse, pageLimit)
  }

}
