package io.kanca.fbgraph

import scalaj.http._
import play.api.libs.json._

case class From(name: String, id: String)

case class MessageTag(
                       id: String,
                       name: String,
                       typ: String,
                       offset: Int,
                       length: Int,
                     )

case class GroupFeed(
                      id: String,
                      caption: Option[String],
                      createdTime: String,
                      description: Option[String],
                      from: From,
                      link: Option[String],
                      message: Option[String],
                      messageTags: List[MessageTag],
                      name: Option[String],
                      permalinkUrl: String,
                      picture: Option[String],
                      statusType: Option[String],
                      story: Option[String],
                      typ: String,
                      updatedTime: String,
                    )

object Graph {

  val FB_URL = sys.env("FB_URL")
  val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt
  val DEFAULT_REQUEST_LIMIT = sys.env("DEFAULT_REQUEST_LIMIT")

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = DEFAULT_PAGE_LIMIT, nextUrl: String = null, results: List[GroupFeed] = List()): List[GroupFeed] = {
    val resString: HttpResponse[String] = Http(s"$FB_URL/$groupId/feed")
      .params(Seq(
        "fields" -> "created_time,id,message,updated_time,caption,story,description,from,link,name,picture,status_type,type,shares,permalink_url,to,message_tags",
        "limit" -> DEFAULT_REQUEST_LIMIT,
        "access_token" -> token
      ))
      .asString

    val rawJson: JsValue = Json.parse(resString.body)
    val data: JsArray = (rawJson \ "data").validate[JsArray].getOrElse(Json.arr())

    val newResults: List[GroupFeed] = results ::: data.validate[JsArray].getOrElse(Json.arr()).as[List[JsObject]]
      .map { rawFeed =>
        GroupFeed(
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
      }

    val next: String = (rawJson \ "paging" \ "next").validate[String].getOrElse(null)

    if (next == null || pageLimit - 1 <= 0) return newResults

    getGroupFeeds(token, groupId, pageLimit - 1, next, newResults)
  }

}
