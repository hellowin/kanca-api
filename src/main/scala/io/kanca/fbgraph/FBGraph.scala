package io.kanca.fbgraph

abstract class FBGraph(defaultPageLimit: Int) {
  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = defaultPageLimit): List[GroupFeed]
}
