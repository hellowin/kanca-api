package io.kanca.core

import io.kanca.core.FBGraphType.{GroupFeed, GroupMember}
import io.kanca.core.ResultType.ResultSortOrder.ResultSortOrder
import io.kanca.core.ResultType.ResultSortType.ResultSortType
import io.kanca.core.ResultType.{GroupFeedResult, ResultSortOrder, ResultSortType}

abstract class Repository {
  def initialize(): Boolean

  def shutdown(): Boolean

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean

  def readGroupFeed(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: ResultSortType = ResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupFeedResult]

  def insertGroupMember(groupId: String, groupMembers: List[GroupMember]): Boolean

  def readGroupMember(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: ResultSortType = ResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupMember]

}
