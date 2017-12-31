package io.kanca.core

import java.time.LocalDateTime

object FBGraphType {

  case class FBListResult[T](
    data: List[T],
    next: Option[String],
  )

  case class From(name: String, id: String)

  case class Shares(count: Int)

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
    shares: Shares,
    var reactions: FBListResult[Reaction],
    var comments: FBListResult[Comment],
  )

  case class Comment(
    id: String,
    from: From,
    permalinkUrl: String,
    message: String,
    createdTime: LocalDateTime,
    var reactions: FBListResult[Reaction],
    var comments: FBListResult[Comment],
  )

  case class Reaction(
    id: String,
    name: String,
    typ: String,
  )

  case class GroupMember(
    id: String,
    name: String,
    pictureUrl: String
  )

}
