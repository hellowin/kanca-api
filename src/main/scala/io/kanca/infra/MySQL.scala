package io.kanca.infra

import java.sql.{Connection, DriverManager}

object MySQL {

  val MYSQL_HOST = sys.env("MYSQL_HOST")
  val MYSQL_PORT = sys.env("MYSQL_PORT")
  val MYSQL_DB = sys.env("MYSQL_DB")
  val MYSQL_USERNAME = sys.env("MYSQL_USERNAME")
  val MYSQL_PASSWORD = sys.env("MYSQL_PASSWORD")
  val DRIVER = "com.mysql.cj.jdbc.Driver"
  val URL = s"jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT"

  @throws[Exception]
  def getConnection: Connection = {
    // make the connection
    Class.forName(DRIVER)
    val connection = DriverManager.getConnection(URL, MYSQL_USERNAME, MYSQL_PASSWORD)

    // create the statement, and run the select query
    val statement = connection.createStatement()
    statement.execute(
      s"""
         |CREATE DATABASE IF NOT EXISTS $MYSQL_DB
         |  CHARACTER SET utf8mb4
         |  COLLATE utf8mb4_unicode_ci
      """.stripMargin
    )

    connection.setCatalog(MYSQL_DB)

    connection
  }

  @throws[Exception]
  def setupTables(connection: Connection): Boolean = {
    val statement = connection.createStatement()

    statement.execute(
      s"""
         |create table if not exists group_feed (
         |	id varchar(255) primary key,
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
         |	picture varchar(255),
         |	status_type varchar(255),
         |	story longtext,
         |	`type` varchar(255),
         |	updated_time datetime
         |)
      """.stripMargin
    )

    true
  }

}
