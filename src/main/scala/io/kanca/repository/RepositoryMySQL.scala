package io.kanca.repository

import io.kanca.core.Repository
import io.kanca.fbgraph.GroupFeed

class RepositoryMySQL(
  conf: ConfigurationMySQL,
  dataSource: DataSourceMySQL,
  groupFeedRepo: GroupFeedMySQL,
) extends Repository {

  def initialize(): Boolean = dataSource.setup()

  def shutdown(): Boolean = dataSource.close()

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = groupFeedRepo.insert(groupFeeds)

  def readGroupFeed(groupId: String, page: Int): List[GroupFeed] = groupFeedRepo.read(groupId, page, conf.readLimit)

}