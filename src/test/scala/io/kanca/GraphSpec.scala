package io.kanca

import org.scalatest._
import io.kanca.fbgraph.{Graph, GroupFeed}

class GraphSpec extends FlatSpec with Matchers {

  val USER_TOKEN = sys.env("USER_TOKEN")
  val GROUP_ID = sys.env("GROUP_ID")
  val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt
  val DEFAULT_REQUEST_LIMIT: Int = sys.env("DEFAULT_REQUEST_LIMIT").toInt

  "Group fetcher" should "able to get feeds by default arguments" in {
    val feeds: List[GroupFeed] = Graph.getGroupFeeds(USER_TOKEN, GROUP_ID)
    feeds.size should be <= DEFAULT_PAGE_LIMIT * DEFAULT_REQUEST_LIMIT
    feeds.size should be > 1 * DEFAULT_REQUEST_LIMIT
  }

  it should "able to get feeds by page limit arguments" in {
    val feeds: List[GroupFeed] = Graph.getGroupFeeds(USER_TOKEN, GROUP_ID, 2)
    feeds should have size 2 * DEFAULT_REQUEST_LIMIT
  }

}
