package io.kanca.fbgraph

import com.twitter.inject.Logging
import play.api.libs.json._

import scalaj.http._

case class FBListResult(data: List[JsObject], next: Option[String])

class Graph extends FBExeption with Logging {

  private val FB_URL = sys.env("FB_URL")
  private val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt
  private val DEFAULT_REQUEST_LIMIT = sys.env("DEFAULT_REQUEST_LIMIT")

  private def getListResult[T](req: HttpRequest, token: String, parser: JsObject => T, pageLimit: Int, results: List[T] = List()): List[T] = {
    val resString: String = req.asString.body
    val rawJson: JsValue = Json.parse(resString)

    // handle error
    checkException(rawJson)

    val data: List[JsObject] = (rawJson \ "data").validate[JsArray].getOrElse(Json.arr()).as[List[JsObject]]
    val next: Option[String] = (rawJson \ "paging" \ "next").validate[String].asOpt

    val newResults: List[T] = results ::: data.map(parser)

    if (next.orNull == null || pageLimit - 1 <= 0) return newResults

    // rebuild request if request is post, token and method params are vanished
    val nextReq: HttpRequest = Http(next.get).params(Seq("method" -> "GET", "access_token" -> token)).postForm

    getListResult[T](nextReq, token, parser, pageLimit - 1, newResults)
  }

  @throws(classOf[FBExeption])
  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = DEFAULT_PAGE_LIMIT): List[GroupFeed] = {
    val req: HttpRequest = Http(s"$FB_URL/$groupId/feed")
      .params(Seq(
        "method" -> "GET",
        "fields" -> "created_time,id,message,updated_time,caption,story,description,from,link,name,picture,status_type,type,shares,permalink_url,to,message_tags",
        "limit" -> DEFAULT_REQUEST_LIMIT,
        "access_token" -> token
      )).postForm

    debug(s"get group feeds url = ${req.url} params = ${req.params.map { case (key: String, value: String) => s"$key = $value" }}")

    getListResult[GroupFeed](req, token, GroupFeed.parse, pageLimit)
  }

}
