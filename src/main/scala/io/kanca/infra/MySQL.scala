package io.kanca.infra

import java.sql.{Connection, DriverManager}

object MySQL {

  val MYSQL_DB = sys.env("MYSQL_DB")
  val MYSQL_USERNAME = sys.env("MYSQL_USERNAME")
  val MYSQL_PASSWORD = sys.env("MYSQL_PASSWORD")
  val DRIVER = "com.mysql.cj.jdbc.Driver"
  val URL = "jdbc:mysql://localhost"

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

}
