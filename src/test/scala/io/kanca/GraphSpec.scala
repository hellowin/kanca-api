package io.kanca

import org.scalatest._
import io.kanca.fbgraph.{Graph, GroupFeed}

class GraphSpec extends FlatSpec with Matchers {

  val USER_TOKEN = sys.env("USER_TOKEN")
  val GROUP_ID = sys.env("GROUP_ID")
  val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt
  val DEFAULT_REQUEST_LIMIT: Int = sys.env("DEFAULT_REQUEST_LIMIT").toInt

  s"Group feeds fetcher with page limit $DEFAULT_PAGE_LIMIT and request limit $DEFAULT_REQUEST_LIMIT" should "able to get feeds by default arguments" in {
    val feeds: List[GroupFeed] = Graph.getGroupFeeds(USER_TOKEN, GROUP_ID)
    feeds.map(_.id).toSet.size should be <= DEFAULT_PAGE_LIMIT * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size should be > 1 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map{ feed => feed.isInstanceOf[GroupFeed] shouldEqual true}
  }

  it should "able to get feeds by page limit arguments" in {
    val feeds: List[GroupFeed] = Graph.getGroupFeeds(USER_TOKEN, GROUP_ID, 2)
    feeds.map(_.id).toSet should have size 2 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map{ feed => feed.isInstanceOf[GroupFeed] shouldEqual true}
  }

}
