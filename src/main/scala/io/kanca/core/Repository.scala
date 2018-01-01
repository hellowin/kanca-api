package io.kanca.core

import java.time.LocalDate

import io.kanca.core.FBGraphType.{GroupFeed, GroupMember}
import io.kanca.core.MetricType.ActivityByDate
import io.kanca.core.ResultType.GroupFeedResultSortType.GroupFeedResultSortType
import io.kanca.core.ResultType.GroupMemberResultSortType.GroupMemberResultSortType
import io.kanca.core.ResultType.ResultSortOrder.ResultSortOrder
import io.kanca.core.ResultType.{GroupFeedResult, GroupFeedResultSortType, GroupMemberResultSortType, ResultSortOrder}

abstract class Repository {
  def initialize(): Boolean

  def shutdown(): Boolean

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean

  def readGroupFeed(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: GroupFeedResultSortType = GroupFeedResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupFeedResult]

  def insertGroupMember(groupId: String, groupMembers: List[GroupMember]): Boolean

  def readGroupMember(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: GroupMemberResultSortType = GroupMemberResultSortType.NAME,
    sortOrder: ResultSortOrder = ResultSortOrder.ASC
  ): List[GroupMember]

  def readActivitiesByDate(groupId: String, dateStart: LocalDate, dateEnd: LocalDate): List[ActivityByDate]

}
