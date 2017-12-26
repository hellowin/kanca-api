package io.kanca.fbgraph

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

trait FBGraphUtils {
  val TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ssZ"
}

case class FBListResult[T](
  data: List[T],
  next: Option[String],
)

object FBListResult extends FBException {

  def parse[T](string: String, parser: JsObject => T): FBListResult[T] = {
    val rawJson: JsValue = Json.parse(string)
    parse(rawJson, parser)
  }

  def parse[T](rawJson: JsValue, parser: JsObject => T): FBListResult[T] = {
    // handle error
    checkException(rawJson)

    val data: List[JsObject] = (rawJson \ "data").validate[JsArray].getOrElse(Json.arr()).as[List[JsObject]]
    val next: Option[String] = (rawJson \ "paging" \ "next").validate[String].asOpt

    val result: List[T] = data.map(parser)

    FBListResult[T](result, next)
  }

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
  var reactions: FBListResult[Reaction],
)

object GroupFeed extends FBGraphUtils {

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
    FBListResult.parse[Reaction]((rawFeed \ "reactions").validate[JsValue].getOrElse(Json.obj()), Reaction.parse _),
  )

}

case class Reaction(
  id: String,
  name: String,
  typ: String,
)

object Reaction {
  def parse(obj: JsObject): Reaction = Reaction(
    (obj \ "id").validate[String].get,
    (obj \ "name").validate[String].get,
    (obj \ "type").validate[String].get,
  )
}
