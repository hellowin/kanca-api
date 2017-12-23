package io.kanca.fbgraph

import java.time.LocalDateTime

import play.api.libs.json.{JsObject, Json}

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
