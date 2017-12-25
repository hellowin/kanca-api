package io.kanca

import com.twitter.inject.Logging
import io.kanca.fbgraph.{FBExeption, GroupFeed}

abstract class Graph extends FBExeption with Logging {
  private val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = DEFAULT_PAGE_LIMIT): List[GroupFeed]
}
