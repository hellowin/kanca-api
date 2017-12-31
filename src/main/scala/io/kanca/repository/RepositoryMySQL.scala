package io.kanca.repository

import io.kanca.core.FBGraphType.{GroupFeed, GroupMember}
import io.kanca.core.Repository
import io.kanca.core.ResultType.GroupFeedResultSortType.GroupFeedResultSortType
import io.kanca.core.ResultType.GroupMemberResultSortType.GroupMemberResultSortType
import io.kanca.core.ResultType.ResultSortOrder.ResultSortOrder
import io.kanca.core.ResultType._

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
    sortBy: GroupFeedResultSortType = GroupFeedResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupFeedResult] = groupFeed.read(groupId, page, limit, sortBy, sortOrder)

  def insertGroupMember(groupId: String, groupMembers: List[GroupMember]): Boolean = groupMember.insert(groupId, groupMembers)

  def readGroupMember(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: GroupMemberResultSortType = GroupMemberResultSortType.NAME,
    sortOrder: ResultSortOrder = ResultSortOrder.ASC
  ): List[GroupMember] = groupMember.read(groupId, page, limit, sortBy, sortOrder)

}