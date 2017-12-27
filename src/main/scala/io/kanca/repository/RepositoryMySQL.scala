package io.kanca.repository

import java.sql.{Connection, DriverManager}

import io.kanca.fbgraph.GroupFeed

class RepositoryMySQL(
  connection: Connection,
  readLimit: Int,
) extends Repository {

  def insertGroupFeed(groupFeeds: List[GroupFeed]): Boolean = GroupFeedMySQL.insert(connection, groupFeeds)

  def readGroupFeed(groupId: String, page: Int): List[GroupFeed] = GroupFeedMySQL.read(connection, readLimit, groupId, page)

}

object RepositoryMySQL {

  @throws[Exception]
  def getConnection(
    host: String,
    port: String,
    database: String,
    username: String,
    password: String,
    driver: String,
  ): Connection = {
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

    connection
  }

  @throws[Exception]
  def setupTables(connection: Connection): Boolean = {
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
         |  reactions json,
         |  reactions_summary json
         |)
      """.stripMargin
    )

    val rs = statement.executeQuery(
      """
        |select count(*) as count from information_schema.statistics where table_name = 'group_feed' and index_name = 'GROUP_ID' and table_schema = database()
      """.stripMargin)

    rs.next()
    val count: Int = rs.getInt("count")
    if (count == 0) {
      statement.execute(
        """
          |ALTER TABLE group_feed ADD INDEX GROUP_ID(group_id asc)
        """.stripMargin
      )
    }

    true
  }

}