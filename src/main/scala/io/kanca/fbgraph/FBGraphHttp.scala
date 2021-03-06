package io.kanca.fbgraph

import io.kanca.core.FBGraph
import io.kanca.core.FBGraphType._
import play.api.libs.json._

import scalaj.http._

class FBGraphHttp(
  version: String,
  connectionTimeout: Int,
  readTimeout: Int,
) extends FBGraph with FBException {

  private val FB_URL: String = s"https://graph.facebook.com/v$version"

  debug(s"Initiated FB Graph API with base URL: $FB_URL")

  private def getListResult[T](req: HttpRequest, token: String, parser: JsObject => T, pageLimit: Int, requestLimit: Int, results: List[T] = List()): FBListResult[T] = {
    val resString: String = req.asString.body
    val listResult: FBListResult[T] = FBListResultParser.parse[T](resString, parser)

    val newResults: List[T] = results ::: listResult.data

    if (listResult.next.orNull == null || pageLimit - 1 <= 0) return FBListResult(newResults, listResult.next)

    // rebuild request if request is post, token and method params are vanished
    val nextReq: HttpRequest = getHttpRequest(token, listResult.next.get, null, requestLimit)

    getListResult[T](nextReq, token, parser, pageLimit - 1, requestLimit, newResults)
  }

  private def getHttpRequest(token: String, node: String, fields: String, limit: Int): HttpRequest = {
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
    Http(url).timeout(connectionTimeout, readTimeout).params(params).postForm
  }

  private def getDeepListResult[T](listResult: FBListResult[T], token: String, parser: JsObject => T, pageLimit: Int, requestLimit: Int): FBListResult[T] = {
    if (listResult.next.orNull == null) {
      listResult
    } else {
      val newListResult: FBListResult[T] = getListResult[T](getHttpRequest(token, listResult.next.get, null, requestLimit), token, parser, pageLimit, requestLimit)
      val data: List[T] = listResult.data ::: newListResult.data
      FBListResult(data, newListResult.next)
    }
  }

  @throws(classOf[Exception])
  def getGroupFeeds(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupFeed] = {
    val req: HttpRequest = getHttpRequest(
      token, s"$groupId/feed",
      s"created_time,id,message,updated_time,caption,story,description,from,link,name,picture,status_type,type,shares,permalink_url,to,message_tags,reactions,comments.limit($requestLimit){comments.limit($requestLimit){created_time,message,from,permalink_url,reactions.limit($requestLimit)},created_time,message,from,permalink_url,reactions.limit($requestLimit)}",
      requestLimit
    )

    debug(s"get group feeds url = ${req.url} params = ${req.params.map { case (key: String, value: String) => s"$key = $value" }}")

    val feedResult: FBListResult[GroupFeed] = getListResult[GroupFeed](req, token, GroupFeedParser.parse, pageLimit, requestLimit)

    // iterate feed reactions, comments, comment's comments, comment's reactions, comment's comment's reactions >:)
    val feeds: List[GroupFeed] = feedResult.data.map(feed => {
      feed.reactions = getDeepListResult[Reaction](feed.reactions, token, ReactionParser.parse, pageLimit, requestLimit)
      feed.comments = getDeepListResult[Comment](feed.comments, token, CommentParser.parse, pageLimit, requestLimit)
      feed.comments = FBListResult(feed.comments.data.map(comment => {
        comment.comments = getDeepListResult[Comment](comment.comments, token, CommentParser.parse, pageLimit, requestLimit)
        comment.reactions = getDeepListResult[Reaction](comment.reactions, token, ReactionParser.parse, pageLimit, requestLimit)
        comment.comments = FBListResult(comment.comments.data.map(comment2 => {
          comment2.reactions = getDeepListResult[Reaction](comment2.reactions, token, ReactionParser.parse, pageLimit, requestLimit)
          comment2
        }), comment.comments.next)
        comment
      }), feed.comments.next)
      feed
    })

    FBListResult(feeds, feedResult.next)
  }

  @throws(classOf[Exception])
  def getGroupMembers(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupMember] = {
    val req: HttpRequest = getHttpRequest(
      token, s"$groupId/members",
      s"id,name,picture{url}",
      requestLimit
    )

    debug(s"get group members url = ${req.url} params = ${req.params.map { case (key: String, value: String) => s"$key = $value" }}")

    getListResult[GroupMember](req, token, GroupMemberParser.parse, pageLimit, requestLimit)
  }

}
