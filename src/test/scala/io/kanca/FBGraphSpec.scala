package io.kanca

import com.twitter.inject.IntegrationTest
import com.twitter.inject.app.TestInjector
import io.kanca.fbgraph.{FBGraph, FBOAuthException, FBGraphModule, GroupFeed}

class FBGraphSpec extends IntegrationTest {

  def injector = TestInjector(
    FBGraphModule
  ).create

  val graph: FBGraph = injector.instance[FBGraph]

  val USER_TOKEN = sys.env("USER_TOKEN")
  val GROUP_ID = sys.env("GROUP_ID")
  val DEFAULT_PAGE_LIMIT: Int = sys.env("DEFAULT_PAGE_LIMIT").toInt
  val DEFAULT_REQUEST_LIMIT: Int = sys.env("DEFAULT_REQUEST_LIMIT").toInt

  test(s"Group feeds fetcher with page limit $DEFAULT_PAGE_LIMIT and request limit $DEFAULT_REQUEST_LIMIT should able to get feeds by default arguments") {
    val feeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID)
    feeds.map(_.id).toSet.size should be <= DEFAULT_PAGE_LIMIT * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size should be > 1 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map { feed => feed.isInstanceOf[GroupFeed] shouldEqual true }
  }

  test("able to get feeds by page limit arguments") {
    val feeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID, 2)
    feeds.map(_.id).toSet should have size 2 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map { feed => feed.isInstanceOf[GroupFeed] shouldEqual true }
  }

  test("able handle wrong token") {
    an[FBOAuthException] should be thrownBy graph.getGroupFeeds("any_token", GROUP_ID)
  }

  test("able handle wrong group id") {
    an[FBOAuthException] should be thrownBy graph.getGroupFeeds(USER_TOKEN, "any_id")
  }

}
