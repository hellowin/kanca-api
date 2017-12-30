package io.kanca.repository

import java.sql.Connection

import com.twitter.inject.app.TestInjector
import com.twitter.inject.{Injector, IntegrationTest}
import io.kanca.core.FBGraphType.GroupFeed
import io.kanca.core.ResultType.GroupFeedResult
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

    repo.initialize()

    val fixtures = graph.getGroupFeeds(USER_TOKEN, DUMMY_GROUP, DEFAULT_PAGE_LIMIT, DEFAULT_REQUEST_LIMIT).data
    repo.insertGroupFeed(fixtures)
  }

  override def afterAll() {
    repo.shutdown()
  }

  test("able to read group feeds with basic parameter") {
    val results: List[GroupFeedResult] = repo.readGroupFeed(DUMMY_GROUP)
    results.size shouldEqual 3
    
    // first item should be message 2 because it has latest updated time
    results.head.id shouldEqual "123456789123456_000000000000002"
  }

}
