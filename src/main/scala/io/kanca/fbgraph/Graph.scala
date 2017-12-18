package io.kanca.fbgraph

import play.api.libs.json._

import scalaj.http._

case class FBListResult(data: List[JsObject], next: Option[String])

object Graph {

  private val FB_URL = sys.env("FB_URL")
  private val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt
  private val DEFAULT_REQUEST_LIMIT = sys.env("DEFAULT_REQUEST_LIMIT")

  private def getListResult[T](req: HttpRequest, token: String, parser: JsObject => T, pageLimit: Int, results: List[T] = List()): List[T] = {
    val resString: String = req.asString.body
    val rawJson: JsValue = Json.parse(resString)
    val data: List[JsObject] = (rawJson \ "data").validate[JsArray].getOrElse(Json.arr()).as[List[JsObject]]
    val next: Option[String] = (rawJson \ "paging" \ "next").validate[String].asOpt

    val newResults: List[T] = results ::: data.map(parser)

    if (next.orNull == null || pageLimit - 1 <= 0) return newResults

    // rebuild request if request is post, token and method params are vanished
    val nextReq: HttpRequest = Http(next.get).params(Seq("method" -> "GET", "access_token" -> token))

    getListResult[T](nextReq, token, parser, pageLimit - 1, newResults)
  }

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = DEFAULT_PAGE_LIMIT): List[GroupFeed] = {
    val req: HttpRequest = Http(s"$FB_URL/$groupId/feed")
      .params(Seq(
        "method" -> "GET",
        "fields" -> "created_time,id,message,updated_time,caption,story,description,from,link,name,picture,status_type,type,shares,permalink_url,to,message_tags",
        "limit" -> DEFAULT_REQUEST_LIMIT,
        "access_token" -> token
      )).postForm

    def parser(rawFeed: JsObject): GroupFeed = GroupFeed(
      (rawFeed \ "id").validate[String].get,
      (rawFeed \ "caption").validate[String].asOpt,
      (rawFeed \ "created_time").validate[String].get,
      (rawFeed \ "description").validate[String].asOpt,
      From(
        (rawFeed \ "from" \ "name").validate[String].get,
        (rawFeed \ "from" \ "id").validate[String].get
      ),
      (rawFeed \ "link").validate[String].asOpt,
      (rawFeed \ "message").validate[String].asOpt,
      (rawFeed \ "message_tags").validate[List[JsObject]].getOrElse(List()).map { obj =>
        MessageTag(
          (obj \ "id").validate[String].get,
          (obj \ "name").validate[String].get,
          (obj \ "type").validate[String].get,
          (obj \ "offset").validate[Int].get,
          (obj \ "length").validate[Int].get
        )
      },
      (rawFeed \ "name").validate[String].asOpt,
      (rawFeed \ "permalink_url").validate[String].get,
      (rawFeed \ "picture").validate[String].asOpt,
      (rawFeed \ "status_type").validate[String].asOpt,
      (rawFeed \ "story").validate[String].asOpt,
      (rawFeed \ "type").validate[String].get,
      (rawFeed \ "updated_time").validate[String].get
    )

    getListResult[GroupFeed](req, token, parser, pageLimit)
  }

}
