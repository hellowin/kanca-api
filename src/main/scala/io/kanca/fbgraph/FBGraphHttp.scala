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

  private def getListResult[T](req: HttpRequest, token: String, parser: JsObject => T, pageLimit: Int = defaultPageLimit, results: List[T] = List()): FBListResult[T] = {
    val resString: String = req.asString.body
    val listResult: FBListResult[T] = FBListResult.parse[T](resString, parser)

    val newResults: List[T] = results ::: listResult.data

    if (listResult.next.orNull == null || pageLimit - 1 <= 0) return FBListResult(newResults,  listResult.next)

    // rebuild request if request is post, token and method params are vanished
    val nextReq: HttpRequest = getHttpRequest(token, listResult.next.get)

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
  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = defaultPageLimit): FBListResult[GroupFeed] = {
    val req: HttpRequest = getHttpRequest(
      token, s"$groupId/feed",
      "created_time,id,message,updated_time,caption,story,description,from,link,name,picture,status_type,type,shares,permalink_url,to,message_tags,reactions"
    )

    debug(s"get group feeds url = ${req.url} params = ${req.params.map { case (key: String, value: String) => s"$key = $value" }}")

    val feedResult: FBListResult[GroupFeed] = getListResult[GroupFeed](req, token, GroupFeed.parse, pageLimit)

    // iterate reactions
    val feeds: List[GroupFeed] = feedResult.data.map(feed => {
      val rea = feed.reactions

      if (feed.reactions.next.orNull == null) {
        feed
      } else {
        val newReactions: List[Reaction] = rea.data ::: getListResult[Reaction](getHttpRequest(token, rea.next.get), token, Reaction.parse).data
        feed.reactions = FBListResult(newReactions, None)
        feed
      }

    })

    FBListResult(feeds, feedResult.next)
  }

}
