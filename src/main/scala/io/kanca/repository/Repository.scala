package io.kanca.repository

import io.kanca.fbgraph.GroupFeed

abstract class Repository {
  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean
}
