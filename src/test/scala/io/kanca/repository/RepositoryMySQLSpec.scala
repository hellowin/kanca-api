package io.kanca.repository

import java.sql.Connection

import com.twitter.inject.app.TestInjector
import com.twitter.inject.{Injector, IntegrationTest}
import io.kanca.core.FBGraphType.{GroupFeed, GroupMember}
import io.kanca.core.{FBGraph, Repository}
import io.kanca.fbgraph.FBGraphMockModule
import org.scalatest.BeforeAndAfterAll

class RepositoryMySQLSpec extends IntegrationTest with BeforeAndAfterAll {

  private val MYSQL_HOST = sys.env.getOrElse("MYSQL_HOST", "localhost")
  private val MYSQL_PORT = sys.env.getOrElse("MYSQL_PORT", "3306")
  private val MYSQL_DATABASE = sys.env.getOrElse("MYSQL_DATABASE", "kanca_api_test")
  private val MYSQL_USERNAME = sys.env("MYSQL_USERNAME")
  private val MYSQL_PASSWORD = sys.env("MYSQL_PASSWORD")
  private val MYSQL_DRIVER = sys.env.getOrElse("MYSQL_DRIVER", "com.mysql.cj.jdbc.Driver")

  private val USER_TOKEN = sys.env("USER_TOKEN")
  private val GROUP_ID = sys.env("GROUP_ID")
  private val READ_LIMIT = sys.env.getOrElse("READ_LIMIT", "100").toInt

  private val DEFAULT_PAGE_LIMIT: Int = 10
  private val DEFAULT_REQUEST_LIMIT: Int = 100

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
  }

  override def afterAll() {
    repo.shutdown()
  }

  test("MySQL Spec should able to get connection") {
    connection.isInstanceOf[Connection] shouldEqual true
  }

  test("GroupFeedRepo should able to insert group feeds batch, multiple times") {
    val groupFeeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    groupFeeds.size should be >= 400
    val res: Boolean = repo.insertGroupFeed(groupFeeds)
    res shouldEqual true

    val res2: Boolean = repo.insertGroupFeed(groupFeeds.take(10))
    res2 shouldEqual true

    val res3: Boolean = repo.insertGroupFeed(groupFeeds.take(10))
    res3 shouldEqual true
  }

  test("able to read group feeds") {
    repo.readGroupFeed(GROUP_ID).size should be >= 100

    repo.readGroupFeed(GROUP_ID, 2).size should be >= 1
  }

  test("GroupMemberRepo should able to insert group member batch, multiple times") {
    val members: List[GroupMember] = graph.getGroupMembers(USER_TOKEN, GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    members.size should be >= 200
    val res: Boolean = repo.insertGroupMember(GROUP_ID, members)
    res shouldEqual true

    val res2: Boolean = repo.insertGroupMember(GROUP_ID, members.take(10))
    res2 shouldEqual true

    val res3: Boolean = repo.insertGroupMember(GROUP_ID, members.take(10))
    res3 shouldEqual true
  }

  test("able to read group members") {
    repo.readGroupMember(GROUP_ID).size should be >= 100

    repo.readGroupMember(GROUP_ID, 2).size should be >= 1
  }

}
