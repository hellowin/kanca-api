package io.kanca.core

import io.kanca.core.FBGraphType.GroupFeed

abstract class Repository {
  def initialize(): Boolean
  def shutdown(): Boolean
  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean
  def readGroupFeed(groupId: String, page: Int = 1, limit: Int = 100): List[GroupFeed]
}
