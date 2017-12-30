package io.kanca.core

import java.time.LocalDateTime

import io.kanca.core.FBGraphType._

object ResultType {

  object ResultSortType extends Enumeration {
    type ResultSortType = Value
    val UPDATED_TIME: ResultSortType = Value
    val CREATED_TIME: ResultSortType = Value
    val TOTAL_REACTIONS: ResultSortType = Value
    val TOTAL_SHARED: ResultSortType = Value
    val TOTAL_COMMENTS: ResultSortType = Value
  }

  object ResultSortOrder extends Enumeration {
    type ResultSortOrder = Value
    val ASC: ResultSortOrder = Value
    val DESC: ResultSortOrder = Value
  }

  case class GroupFeedResult(
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
    reactions: List[Reaction],
    reactionsSummary: List[ReactionSummary],
    var comments: List[CommentResult],
  )

  case class CommentResult(
    id: String,
    feedId: String,
    parentId: Option[String],
    from: From,
    permalinkUrl: String,
    message: String,
    createdTime: LocalDateTime,
    reactions: List[Reaction],
    reactionsSummary: List[ReactionSummary],
    var comments: List[CommentResult],
  )

  case class ReactionSummary(
    typ: String,
    count: Int
  )

}
