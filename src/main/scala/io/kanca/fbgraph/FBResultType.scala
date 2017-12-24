package io.kanca.fbgraph

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import play.api.libs.json.JsObject

class FBGraph {
  val TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ssZ"
}

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
  createdTime: LocalDateTime,
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
  updatedTime: LocalDateTime,
)

object GroupFeed extends FBGraph {

  def parse(rawFeed: JsObject): GroupFeed = GroupFeed(
    (rawFeed \ "id").validate[String].get,
    (rawFeed \ "caption").validate[String].asOpt,
    LocalDateTime.parse((rawFeed \ "created_time").validate[String].get, DateTimeFormatter ofPattern TIME_FORMATTER),
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
    LocalDateTime.parse((rawFeed \ "updated_time").validate[String].get, DateTimeFormatter ofPattern TIME_FORMATTER),
  )

}
