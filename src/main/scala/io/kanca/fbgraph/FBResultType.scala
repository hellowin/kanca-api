package io.kanca.fbgraph

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
