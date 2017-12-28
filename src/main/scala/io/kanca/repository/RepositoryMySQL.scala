package io.kanca.repository

import java.sql.{Connection, DriverManager, Statement}

import com.twitter.inject.Logging
import com.twitter.util.{Duration, Stopwatch}
import io.kanca.fbgraph.GroupFeed

class RepositoryMySQL(
  connection: Connection,
  readLimit: Int,
) extends Repository {

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = GroupFeedMySQL.insert(connection, groupFeeds)

  def readGroupFeed(groupId: String, page: Int): List[GroupFeed] = GroupFeedMySQL.read(connection, readLimit, groupId, page)

}

object RepositoryMySQL extends Logging {

  @throws[Exception]
  def getConnection(
    host: String,
    port: String,
    database: String,
    username: String,
    password: String,
    driver: String,
  ): Connection = {
    val elapsed: () => Duration = Stopwatch.start()
    val URL = s"jdbc:mysql://$host:$port"

    // make the connection
    Class.forName(driver)
    val connection = DriverManager.getConnection(URL, username, password)

    // create the statement, and run the select query
    val statement = connection.createStatement()
    // utf8mb4 is necessary because existence of emotikon on some post's message
    statement.execute(
      s"""
         |CREATE DATABASE IF NOT EXISTS $database
         |  CHARACTER SET utf8mb4
         |  COLLATE utf8mb4_unicode_ci
      """.stripMargin
    )

    connection.setCatalog(database)

    debug(s"MySQL get connection time lapsed: ${elapsed().inMilliseconds.toString} ms")

    connection
  }

  def addIndex(statement: Statement, tableName: String, indexName: String, indexStatement: String): Unit = {
    val rs = statement.executeQuery(
      s"""
        |select count(*) as count from information_schema.statistics where table_name = '$tableName' and index_name = '$indexName' and table_schema = database()
      """.stripMargin)

    rs.next()
    val count: Int = rs.getInt("count")
    if (count == 0) {
      statement.execute(
        s"""
          |ALTER TABLE $tableName ADD INDEX $indexName($indexStatement)
        """.stripMargin
      )
    }
  }

  @throws[Exception]
  def setupTables(connection: Connection): Boolean = {
    val elapsed: () => Duration = Stopwatch.start()
    val statement = connection.createStatement()

    statement.execute(
      s"""
         |create table if not exists group_feed (
         |	id varchar(255) primary key,
         |  group_id varchar(255) not null,
         |  caption varchar(255),
         |	created_time datetime,
         |	description longtext,
         |	from_name varchar(255),
         |	from_id varchar(255),
         |	link varchar(255),
         |	message longtext,
         |	message_tags json,
         |	`name` varchar(255),
         |	permalink_url varchar(255),
         |	picture longtext,
         |	status_type varchar(255),
         |	story longtext,
         |	`type` varchar(255),
         |	updated_time datetime,
         |  shares_count integer,
         |  reactions json,
         |  reactions_summary json
         |)
      """.stripMargin
    )

    addIndex(statement, "group_feed", "GROUP_ID", "group_id asc")

    statement.execute(
      s"""
         |create table if not exists group_comment (
         |	id varchar(255) primary key,
         |  group_id varchar(255) not null,
         |  feed_id varchar(255) not null,
         |  parent_id varchar(255),
         |	created_time datetime,
         |	from_name varchar(255),
         |	from_id varchar(255),
         |	message longtext,
         |	permalink_url varchar(255),
         |  reactions json,
         |  reactions_summary json
         |)
      """.stripMargin
    )

    addIndex(statement, "group_comment", "GROUP_ID", "group_id asc")
    addIndex(statement, "group_comment", "FEED_ID", "feed_id asc")
    addIndex(statement, "group_comment", "PARENT_ID", "parent_id asc")

    debug(s"MySQL setup table time: ${elapsed().inMilliseconds.toString} ms")

    true
  }

}