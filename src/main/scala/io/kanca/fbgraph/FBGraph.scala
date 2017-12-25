package io.kanca.fbgraph

abstract class FBGraph {
  val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = DEFAULT_PAGE_LIMIT): List[GroupFeed]
}
