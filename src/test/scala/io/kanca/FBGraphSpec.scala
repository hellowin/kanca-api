package io.kanca

import com.twitter.inject.{Injector, IntegrationTest}
import com.twitter.inject.app.TestInjector
import io.kanca.fbgraph.{FBGraph, FBGraphModule, FBOAuthException, GroupFeed}

class FBGraphSpec extends IntegrationTest {

  private val USER_TOKEN = sys.env("USER_TOKEN")
  private val GROUP_ID = sys.env("GROUP_ID")
  private val FB_GRAPH_VERSION: String = sys.env.getOrElse("FB_GRAPH_VERSION", "2.11")
  private val FB_GRAPH_VERSION_OLD: String = sys.env.getOrElse("FB_GRAPH_VERSION_OLD", "2.8")
  private val DEFAULT_PAGE_LIMIT: Int = sys.env.getOrElse("DEFAULT_PAGE_LIMIT", "10").toInt
  private val DEFAULT_REQUEST_LIMIT: Int = sys.env.getOrElse("DEFAULT_REQUEST_LIMIT", "100").toInt

  def injector: Injector = TestInjector(
    flags = Map(
      "fbgraph.version" -> FB_GRAPH_VERSION,
      "fbgraph.defaultPageLimit" -> DEFAULT_PAGE_LIMIT.toString,
      "fbgraph.defaultRequestLimit" -> DEFAULT_REQUEST_LIMIT.toString,
    ),
    modules = Seq(FBGraphModule),
  ).create

  def injectorOld: Injector = TestInjector(
    flags = Map(
      "fbgraph.version" -> FB_GRAPH_VERSION_OLD,
    ),
    modules = Seq(FBGraphModule),
  ).create

  val graph: FBGraph = injector.instance[FBGraph]
  val graphOld: FBGraph = injectorOld.instance[FBGraph]

  test(s"Group feeds fetcher with page limit $DEFAULT_PAGE_LIMIT and request limit $DEFAULT_REQUEST_LIMIT should able to get feeds by default arguments") {
    val feeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID)
    feeds.map(_.id).toSet.size should be <= DEFAULT_PAGE_LIMIT * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size should be > 1 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map { feed => feed.isInstanceOf[GroupFeed] shouldEqual true }
  }

  test(s"Able to get from old API version $FB_GRAPH_VERSION_OLD") {
    val feeds: List[GroupFeed] = graphOld.getGroupFeeds(USER_TOKEN, GROUP_ID)
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
