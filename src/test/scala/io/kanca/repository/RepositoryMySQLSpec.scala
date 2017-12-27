package io.kanca.repository

import java.sql.Connection

import com.twitter.inject.app.TestInjector
import com.twitter.inject.{Injector, IntegrationTest}
import io.kanca.fbgraph.{FBGraph, FBGraphMockModule, GroupFeed}
import org.scalatest.BeforeAndAfterAll

class RepositoryMySQLSpec extends IntegrationTest with BeforeAndAfterAll {

  private val MYSQL_HOST = sys.env.getOrElse("MYSQL_HOST", "localhost")
  private val MYSQL_PORT = sys.env.getOrElse("MYSQL_PORT", "3306")
  private val MYSQL_DATABASE = sys.env.getOrElse("MYSQL_DATABASE", "kanca_api")
  private val MYSQL_USERNAME = sys.env("MYSQL_USERNAME")
  private val MYSQL_PASSWORD = sys.env("MYSQL_PASSWORD")
  private val MYSQL_DRIVER = sys.env.getOrElse("MYSQL_DRIVER", "com.mysql.cj.jdbc.Driver")

  private val USER_TOKEN = sys.env("USER_TOKEN")
  private val GROUP_ID = sys.env("GROUP_ID")
  private val READ_LIMIT = sys.env.getOrElse("READ_LIMIT", "100").toInt
  private val connection = RepositoryMySQL.getConnection(MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DRIVER)

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

  override def beforeAll() {
    val statement = connection.createStatement()

    statement.execute(
      """
        |DROP TABLE IF EXISTS group_feed
      """.stripMargin)

    RepositoryMySQL.setupTables(connection)
  }

  test("MySQL Spec should able to get connection") {
    connection.isInstanceOf[Connection] shouldEqual true
  }

  test("able to setup tables, multiple times") {
    RepositoryMySQL.setupTables(connection) shouldEqual true
    RepositoryMySQL.setupTables(connection) shouldEqual true
  }

  test("GroupFeedRepo should able to insert group feeds batch, multiple times") {
    val groupFeeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    groupFeeds.size should be >= 400
    val res: Boolean = repo.insertGroupFeed(groupFeeds)
    res shouldEqual true

    val res2: Boolean = repo.insertGroupFeed(groupFeeds)
    res2 shouldEqual true
  }

  test("able to read group feeds") {
    repo.readGroupFeed(GROUP_ID).size should be >= 100

    repo.readGroupFeed(GROUP_ID, 2).size should be >= 1
  }

}
