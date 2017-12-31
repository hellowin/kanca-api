package io.kanca.fbgraph

import com.twitter.inject.app.TestInjector
import com.twitter.inject.{Injector, IntegrationTest}
import io.kanca.core.FBGraph
import io.kanca.core.FBGraphType.{GroupFeed, GroupMember}

class FBGraphSpec extends IntegrationTest {

  private val USER_TOKEN = sys.env("USER_TOKEN")
  private val GROUP_ID = sys.env("GROUP_ID")
  private val FB_GRAPH_VERSION: String = sys.env.getOrElse("FB_GRAPH_VERSION", "2.11")
  private val FB_GRAPH_VERSION_OLD: String = sys.env.getOrElse("FB_GRAPH_VERSION_OLD", "2.8")
  private val FB_GRAPH_CONNECTION_TIMEOUT: Int = sys.env.getOrElse("FB_GRAPH_CONNECTION_TIMEOUT", "2000").toInt
  private val FB_GRAPH_READ_TIMEOUT: Int = sys.env.getOrElse("FB_GRAPH_READ_TIMEOUT", "10000").toInt
  private val DEFAULT_PAGE_LIMIT: Int = sys.env.getOrElse("DEFAULT_PAGE_LIMIT", "10").toInt
  private val DEFAULT_REQUEST_LIMIT: Int = sys.env.getOrElse("DEFAULT_REQUEST_LIMIT", "100").toInt

  def injector: Injector = TestInjector(
    flags = Map(
      "fbgraph.version" -> FB_GRAPH_VERSION,
    ),
    modules = Seq(FBGraphModule),
  ).create

  def injectorOld: Injector = TestInjector(
    flags = Map(
      "fbgraph.version" -> FB_GRAPH_VERSION_OLD,
      "fbgraph.connectionTimeout" -> FB_GRAPH_CONNECTION_TIMEOUT.toString,
      "fbgraph.readTimeout" -> FB_GRAPH_READ_TIMEOUT.toString,
    ),
    modules = Seq(FBGraphModule),
  ).create

  val graph: FBGraph = injector.instance[FBGraph]
  val graphOld: FBGraph = injectorOld.instance[FBGraph]

  test(s"Group feeds fetcher with page limit $DEFAULT_PAGE_LIMIT and request limit $DEFAULT_REQUEST_LIMIT should able to get feeds by default arguments") {
    val feeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    feeds.map(_.id).toSet.size should be <= DEFAULT_PAGE_LIMIT * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size should be > 1 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map { feed => feed.isInstanceOf[GroupFeed] shouldEqual true }
  }

  test(s"Able to get from old API version $FB_GRAPH_VERSION_OLD") {
    val feeds: List[GroupFeed] = graphOld.getGroupFeeds(USER_TOKEN, GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    feeds.map { feed => feed.isInstanceOf[GroupFeed] shouldEqual true }
  }

  test("able to get feeds by page limit arguments") {
    val feeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID, 2, DEFAULT_REQUEST_LIMIT).data
    feeds.map(_.id).toSet should have size 2 * DEFAULT_REQUEST_LIMIT
    feeds.map(_.id).toSet.size shouldEqual feeds.size
    feeds.map { feed => feed.isInstanceOf[GroupFeed] shouldEqual true }
  }

  test("able handle wrong token") {
    an[FBOAuthException] should be thrownBy graph.getGroupFeeds("any_token", GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT)
  }

  test("able handle wrong group id") {
    an[FBOAuthException] should be thrownBy graph.getGroupFeeds(USER_TOKEN, "any_id", DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT)
  }

  test(s"Group members fetcher") {
    val members: List[GroupMember] = graph.getGroupMembers(USER_TOKEN, GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    members.map(_.id).toSet.size should be <= DEFAULT_PAGE_LIMIT * DEFAULT_REQUEST_LIMIT
    members.map(_.id).toSet.size should be > 1 * DEFAULT_REQUEST_LIMIT
    members.map(_.id).toSet.size shouldEqual members.size
    members.map { member => member.isInstanceOf[GroupMember] shouldEqual true }
  }

}
