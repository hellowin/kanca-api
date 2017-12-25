package io.kanca.repository

import io.kanca.fbgraph.GroupFeed

abstract class Repository {
  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean
  def readGroupFeed(groupId: String, page: Int = 1): List[GroupFeed]
}
