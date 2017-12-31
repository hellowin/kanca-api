package io.kanca.fbgraph

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.kanca.core.FBGraphType._
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

trait FBGraphUtils {
  val TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ssZ"
}

object FBListResultParser extends FBException {

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

object GroupFeedParser extends FBGraphUtils {

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
    Shares(
      (rawFeed \ "shares" \ "count").validate[Int].getOrElse(0),
    ),
    FBListResultParser.parse[Reaction]((rawFeed \ "reactions").validate[JsValue].getOrElse(Json.obj()), ReactionParser.parse _),
    FBListResultParser.parse[Comment]((rawFeed \ "comments").validate[JsValue].getOrElse(Json.obj()), CommentParser.parse _),
  )

}

object CommentParser extends FBGraphUtils {
  def parse(obj: JsObject): Comment = Comment(
    (obj \ "id").validate[String].get,
    From(
      (obj \ "from" \ "name").validate[String].get,
      (obj \ "from" \ "id").validate[String].get
    ),
    (obj \ "permalink_url").validate[String].get,
    (obj \ "message").validate[String].get,
    LocalDateTime.parse((obj \ "created_time").validate[String].get, DateTimeFormatter ofPattern TIME_FORMATTER),
    FBListResultParser.parse[Reaction]((obj \ "reactions").validate[JsValue].getOrElse(Json.obj()), ReactionParser.parse _),
    FBListResultParser.parse[Comment]((obj \ "comments").validate[JsValue].getOrElse(Json.obj()), CommentParser.parse _),
  )
}

object ReactionParser {
  def parse(obj: JsObject): Reaction = Reaction(
    (obj \ "id").validate[String].get,
    (obj \ "name").validate[String].get,
    (obj \ "type").validate[String].get,
  )
}

object GroupMemberParser {
  def parse(obj: JsObject): GroupMember = GroupMember(
    (obj \ "id").validate[String].get,
    (obj \ "name").validate[String].get,
    (obj \ "picture" \ "data" \ "url").validate[String].get,
  )
}