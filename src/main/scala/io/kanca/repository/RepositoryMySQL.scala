package io.kanca.repository

import io.kanca.core.FBGraphType.{GroupFeed, GroupMember}
import io.kanca.core.Repository
import io.kanca.core.ResultType.ResultSortOrder.ResultSortOrder
import io.kanca.core.ResultType.ResultSortType.ResultSortType
import io.kanca.core.ResultType.{GroupFeedResult, ResultSortOrder, ResultSortType}

class RepositoryMySQL(
  conf: ConfigurationMySQL,
  dataSource: DataSourceMySQL,
  groupFeed: GroupFeedMySQL,
  groupMember: GroupMemberMySQL

) extends Repository {

  def initialize(): Boolean = dataSource.setup()

  def shutdown(): Boolean = dataSource.close()

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = groupFeed.insert(groupFeeds)

  def readGroupFeed(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: ResultSortType = ResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupFeedResult] = groupFeed.read(groupId, page, limit, sortBy, sortOrder)

  def insertGroupMember(groupId: String, groupMembers: List[GroupMember]): Boolean = groupMember.insert(groupId, groupMembers)

  def readGroupMember(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: ResultSortType = ResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupMember] = groupMember.read(groupId, page, limit, sortBy, sortOrder)

}