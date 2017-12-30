package io.kanca.repository

import io.kanca.core.FBGraphType.GroupFeed
import io.kanca.core.Repository
import io.kanca.core.ResultType.ResultSortOrder.ResultSortOrder
import io.kanca.core.ResultType.ResultSortType.ResultSortType
import io.kanca.core.ResultType.{GroupFeedResult, ResultSortOrder, ResultSortType}

class RepositoryMySQL(
  conf: ConfigurationMySQL,
  dataSource: DataSourceMySQL,
  groupFeedRepo: GroupFeedMySQL,
) extends Repository {

  def initialize(): Boolean = dataSource.setup()

  def shutdown(): Boolean = dataSource.close()

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = groupFeedRepo.insert(groupFeeds)

  def readGroupFeed(
    groupId: String,
    page: Int = 1,
    limit: Int = 100,
    sortBy: ResultSortType = ResultSortType.UPDATED_TIME,
    sortOrder: ResultSortOrder = ResultSortOrder.DESC
  ): List[GroupFeedResult] = groupFeedRepo.read(groupId, page, limit, sortBy, sortOrder)

}