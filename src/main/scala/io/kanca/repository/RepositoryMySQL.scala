package io.kanca.repository

import io.kanca.fbgraph.GroupFeed

class RepositoryMySQL(
  groupFeedRepo: GroupFeedMySQL,
  conf: MySQLConfiguration,
) extends Repository {

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = groupFeedRepo.insert(groupFeeds)

  def readGroupFeed(groupId: String, page: Int): List[GroupFeed] = groupFeedRepo.read(groupId, page, conf.readLimit)

}