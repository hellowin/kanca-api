package io.kanca

import java.sql.Connection

import io.kanca.fbgraph.{Graph, GroupFeed}
import io.kanca.infra.MySQL
import io.kanca.repository.GroupFeedRepo
import org.scalatest.{FlatSpec, Matchers}

class MySQLSpec extends FlatSpec with Matchers {

  val USER_TOKEN = sys.env("USER_TOKEN")
  val GROUP_ID = sys.env("GROUP_ID")
  val READ_LIMIT = sys.env("READ_LIMIT").toInt
  val connection = MySQL.getConnection

  "MySQL Spec" should "able to get connection" in {
    connection.isInstanceOf[Connection] shouldEqual true
  }

  it should "able to setup tables" in {
    MySQL.setupTables(connection) shouldEqual true
  }

  "GroupFeedRepo" should "able to insert group feeds batch" in {
    val groupFeeds: List[GroupFeed] = Graph.getGroupFeeds(USER_TOKEN, GROUP_ID)
    val res: Boolean = GroupFeedRepo.insert(connection, groupFeeds)
    res shouldEqual true
  }

  it should "able to read group feeds with default arguments" in {
    val groupFeeds: List[GroupFeed] = GroupFeedRepo.read(connection)
    groupFeeds.size shouldEqual READ_LIMIT
  }

}
