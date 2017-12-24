package io.kanca

import java.sql.Connection

import com.twitter.inject.IntegrationTest
import com.twitter.inject.app.TestInjector
import io.kanca.fbgraph.{Graph, GroupFeed}
import io.kanca.infra.MySQL
import io.kanca.repository.GroupFeedRepo

class MySQLSpec extends IntegrationTest {

  def injector =
    TestInjector(
      modules =
        Seq(GraphTestModule))
      .create

  val graph = injector.instance[Graph]

  val USER_TOKEN = sys.env("USER_TOKEN")
  val GROUP_ID = sys.env("GROUP_ID")
  val READ_LIMIT = sys.env("READ_LIMIT").toInt
  val connection = MySQL.getConnection

  test("MySQL Spec should able to get connection") {
    connection.isInstanceOf[Connection] shouldEqual true
  }

  test("able to setup tables, multiple times") {
    MySQL.setupTables(connection) shouldEqual true
    MySQL.setupTables(connection) shouldEqual true
  }

  test("GroupFeedRepo should able to insert group feeds batch, multiple times") {
    val groupFeeds: List[GroupFeed] = graph.getGroupFeeds(USER_TOKEN, GROUP_ID)
    val res: Boolean = GroupFeedRepo.insert(connection, groupFeeds)
    res shouldEqual true

    val res2: Boolean = GroupFeedRepo.insert(connection, groupFeeds)
    res2 shouldEqual true
  }

  test("able to read group feeds") {
    val groupFeeds: List[GroupFeed] = GroupFeedRepo.read(connection, GROUP_ID)
    groupFeeds.size shouldEqual READ_LIMIT

    val groupFeedsPage2: List[GroupFeed] = GroupFeedRepo.read(connection, GROUP_ID, 2)
    groupFeedsPage2.size should be > 1
  }

}
