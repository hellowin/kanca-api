package io.kanca.repository

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.twitter.inject.app.TestInjector
import com.twitter.inject.{Injector, IntegrationTest}
import io.kanca.core.FBGraphType.GroupMember
import io.kanca.core.ResultType.{GroupFeedResult, GroupFeedResultSortType, GroupMemberResultSortType, ResultSortOrder}
import io.kanca.core.{FBGraph, Repository}
import io.kanca.fbgraph.FBGraphMockModule
import org.scalatest.BeforeAndAfterAll

class RepositorySpec extends IntegrationTest with BeforeAndAfterAll {

  private val MYSQL_HOST = sys.env.getOrElse("MYSQL_HOST", "localhost")
  private val MYSQL_PORT = sys.env.getOrElse("MYSQL_PORT", "3306")
  private val MYSQL_DATABASE = sys.env.getOrElse("MYSQL_DATABASE", "kanca_api_test")
  private val MYSQL_USERNAME = sys.env("MYSQL_USERNAME")
  private val MYSQL_PASSWORD = sys.env("MYSQL_PASSWORD")
  private val MYSQL_DRIVER = sys.env.getOrElse("MYSQL_DRIVER", "com.mysql.cj.jdbc.Driver")

  private val USER_TOKEN = sys.env("USER_TOKEN")
  private val READ_LIMIT = sys.env.getOrElse("READ_LIMIT", "100").toInt

  private val DEFAULT_PAGE_LIMIT: Int = 1
  private val DEFAULT_REQUEST_LIMIT: Int = 100

  private val DUMMY_GROUP = "123456789123456"

  def injector: Injector = TestInjector(
    flags = Map(
      "repo.mysql.host" -> MYSQL_HOST,
      "repo.mysql.port" -> MYSQL_PORT,
      "repo.mysql.database" -> MYSQL_DATABASE,
      "repo.mysql.username" -> MYSQL_USERNAME,
      "repo.mysql.password" -> MYSQL_PASSWORD,
      "repo.mysql.driver" -> MYSQL_DRIVER,
      "repo.readLimit" -> READ_LIMIT.toString,
    ),
    modules = Seq(
      RepoModuleMySQL,
      FBGraphMockModule,
    ),
  ).create

  private val graph = injector.instance[FBGraph]
  private val repo = injector.instance[Repository]
  private val dataSource = injector.instance[DataSourceMySQL]
  private val connection = dataSource.getConnection

  override def beforeAll() {
    val statement = connection.createStatement()

    statement.execute(
      """
        |DROP TABLE IF EXISTS group_feed
      """.stripMargin)

    statement.execute(
      """
        |DROP TABLE IF EXISTS group_comment
      """.stripMargin)

    statement.execute(
      """
        |DROP TABLE IF EXISTS group_member
      """.stripMargin)

    statement.execute(
      """
        |DROP TABLE IF EXISTS group_member_membership
      """.stripMargin)

    repo.initialize()

    val groupFeeds = graph.getGroupFeeds(USER_TOKEN, DUMMY_GROUP, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    repo.insertGroupFeed(groupFeeds)

    val groupMembers = graph.getGroupMembers(USER_TOKEN, DUMMY_GROUP, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    repo.insertGroupMember(DUMMY_GROUP, groupMembers)
  }

  override def afterAll() {
    repo.shutdown()
  }

  test("able to read group feeds with basic parameter") {
    val results: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP)
    results.size shouldEqual 4

    // first item should be message 2 because it has latest updated time
    results.head.id shouldEqual "123456789123456_000000000000002"

    results.map(_.id).mkString(", ") shouldEqual "123456789123456_000000000000002, 123456789123456_000000000000004, 123456789123456_000000000000003, 123456789123456_000000000000001"
  }

  test("able to read group feeds with pagination and limit") {

    // get page 1, limit 3
    val page1: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP, 1, 3)
    page1.size shouldEqual 3

    // first item should be message 2 because it has latest updated time
    val first = page1.head
    val last = page1.last

    first.id shouldEqual "123456789123456_000000000000002"

    last.id shouldEqual "123456789123456_000000000000003"

    page1.map(_.id).mkString(", ") shouldEqual "123456789123456_000000000000002, 123456789123456_000000000000004, 123456789123456_000000000000003"

    // get page 2, limit 3
    val page2: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP, 2, 3)
    page2.size shouldEqual 1

    // first item should be message 2 because it has latest updated time
    page2.head.id shouldEqual "123456789123456_000000000000001"

    page2.map(_.id).mkString(", ") shouldEqual "123456789123456_000000000000001"
  }

  test("able to read group feeds with sort type and order") {

    // get page 1, limit 3, by created at
    val page1: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP, 1, 3, GroupFeedResultSortType.CREATED_TIME)
    page1.size shouldEqual 3

    // first item should be message 2 because it has latest updated time
    val first = page1.head
    val last = page1.last

    first.id shouldEqual "123456789123456_000000000000004"

    last.id shouldEqual "123456789123456_000000000000002"

    page1.map(_.id).mkString(", ") shouldEqual "123456789123456_000000000000004, 123456789123456_000000000000003, 123456789123456_000000000000002"

    // get page 2, limit 3
    val page2: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP, 2, 3, GroupFeedResultSortType.CREATED_TIME)
    page2.size shouldEqual 1

    // first item should be message 2 because it has latest updated time
    page2.head.id shouldEqual "123456789123456_000000000000001"

    page2.map(_.id).mkString(", ") shouldEqual "123456789123456_000000000000001"

    // get page 1, limit 3, by created at, asc
    val page1a: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP, 1, 3, GroupFeedResultSortType.CREATED_TIME, ResultSortOrder.ASC)
    page1a.size shouldEqual 3

    // first item should be message 2 because it has latest updated time
    val firsta = page1a.head
    val lasta = page1a.last

    firsta.id shouldEqual "123456789123456_000000000000001"

    lasta.id shouldEqual "123456789123456_000000000000003"

    page1a.map(_.id).mkString(", ") shouldEqual "123456789123456_000000000000001, 123456789123456_000000000000002, 123456789123456_000000000000003"
  }

  test("able to read group members with basic parameter") {
    val results: List[GroupMember] = repo.readGroupMember(DUMMY_GROUP)
    results.size shouldEqual 4

    // first item should be message 2 because it has latest updated time
    results.head.id shouldEqual "12345678900000001"

    results.map(_.id).mkString(", ") shouldEqual "12345678900000001, 12345678900000002, 12345678900000003, 12345678900000004"
  }

  test("able to read group members with pagination and limit") {

    // get page 1, limit 3
    val page1: List[GroupMember] = repo.readGroupMember(DUMMY_GROUP, 1, 3)
    page1.size shouldEqual 3

    val first = page1.head
    val last = page1.last

    first.id shouldEqual "12345678900000001"

    last.id shouldEqual "12345678900000003"

    page1.map(_.id).mkString(", ") shouldEqual "12345678900000001, 12345678900000002, 12345678900000003"

    // get page 2, limit 3
    val page2: List[GroupMember] = repo.readGroupMember(DUMMY_GROUP, 2, 3)
    page2.size shouldEqual 1

    // first item should be message 2 because it has latest updated time
    page2.head.id shouldEqual "12345678900000004"

    page2.map(_.id).mkString(", ") shouldEqual "12345678900000004"
  }

  test("able to read group members with sort type and order") {

    // get page 1, limit 3, by created at
    val page1: List[GroupMember] = repo.readGroupMember(DUMMY_GROUP, 1, 3, GroupMemberResultSortType.NAME)
    page1.size shouldEqual 3

    val first = page1.head
    val last = page1.last

    first.id shouldEqual "12345678900000001"

    last.id shouldEqual "12345678900000003"

    page1.map(_.id).mkString(", ") shouldEqual "12345678900000001, 12345678900000002, 12345678900000003"

    // get page 1, limit 3, by created at, desc
    val page2: List[GroupMember] = repo.readGroupMember(DUMMY_GROUP, 1, 3, GroupMemberResultSortType.NAME, ResultSortOrder.DESC)
    page1.size shouldEqual 3

    val firsta = page2.head
    val lasta = page2.last

    firsta.id shouldEqual "12345678900000004"

    lasta.id shouldEqual "12345678900000002"

    page2.map(_.id).mkString(", ") shouldEqual "12345678900000004, 12345678900000003, 12345678900000002"
  }

  test("test metrics activities by date") {
    val activitiesMyDate = repo.readActivitiesByDate(DUMMY_GROUP, LocalDate.now().minusYears(1), LocalDate.now())
    println(activitiesMyDate)
    activitiesMyDate.size shouldEqual 2
    activitiesMyDate.head.feedsCount shouldEqual 3
    activitiesMyDate.head.commentsCount shouldEqual 2
    activitiesMyDate.head.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) shouldEqual "2017-01-01"
  }

}
