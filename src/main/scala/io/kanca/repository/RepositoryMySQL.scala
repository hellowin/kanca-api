package io.kanca.repository

import com.google.inject.Inject
import io.kanca.fbgraph.GroupFeed

class RepositoryMySQL @Inject()(
  groupFeedRepo: GroupFeedMySQL,
  readLimit: Int,
) extends Repository {

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = groupFeedRepo.insert(groupFeeds)

  def readGroupFeed(groupId: String, page: Int): List[GroupFeed] = groupFeedRepo.read(groupId, page, readLimit)

}