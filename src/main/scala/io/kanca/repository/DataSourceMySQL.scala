package io.kanca.repository

import java.sql.{Connection, Statement}

import com.google.inject.{Inject, Singleton}
import com.twitter.inject.Logging
import com.twitter.util.{Duration, Stopwatch}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

@Singleton
class DataSourceMySQL @Inject()(
  conf: ConfigurationMySQL,
) extends Logging {

  Class.forName(conf.driver)

  val config = new HikariConfig()
  config.setJdbcUrl(s"jdbc:mysql://${conf.host}:${conf.port}/${conf.database}?useSSL=${if (conf.useSSL) "true" else "false"}")
  config.setUsername(conf.username)
  config.setPassword(conf.password)
  config.setConnectionTimeout(conf.connectionTimeout)
  config.setMaximumPoolSize(conf.connectionPoolSize)

  val ds: HikariDataSource = new HikariDataSource(config)

  @throws[Exception]
  def getConnection: Connection = ds.getConnection()

  def addIndex(statement: Statement, tableName: String, indexName: String, indexStatement: String): Unit = {
    val rs = statement.executeQuery(
      s"""
         |select count(*) as count from information_schema.statistics where table_name = '$tableName' and index_name = '$indexName' and table_schema = database()
      """.stripMargin)

    rs.next()
    val count: Int = rs.getInt("count")
    if (count == 0) {
      try {
        statement.execute(
          s"""
             |ALTER TABLE $tableName ADD INDEX $indexName($indexStatement)
        """.stripMargin
        )
      } catch {
        case _: Exception => // no-op
      }
    }
  }

  @throws[Exception]
  def setup(): Boolean = {
    val GROUP_FEED_TABLE = "group_feed"
    val GROUP_COMMENT_TABLE = "group_comment"
    val GROUP_MEMBER_TABLE = "group_member"
    val GROUP_MEMBER_MEMBERSHIP_TABLE = "group_member_membership"

    val elapsed: () => Duration = Stopwatch.start()

    val statement = ds.getConnection.createStatement()

    statement.execute(
      s"""
         |create table if not exists $GROUP_FEED_TABLE (
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

    addIndex(statement, GROUP_FEED_TABLE, "GROUP_ID", "group_id asc")
    addIndex(statement, GROUP_FEED_TABLE, "UPDATED_TIME_DESC", "updated_time desc")

    statement.execute(
      s"""
         |create table if not exists $GROUP_COMMENT_TABLE (
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

    addIndex(statement, GROUP_COMMENT_TABLE, "GROUP_ID", "group_id asc")
    addIndex(statement, GROUP_COMMENT_TABLE, "FEED_ID", "feed_id asc")
    addIndex(statement, GROUP_COMMENT_TABLE, "PARENT_ID", "parent_id asc")

    statement.execute(
      s"""
         |create table if not exists $GROUP_MEMBER_TABLE (
         |	id varchar(255) primary key,
         |  name longtext,
         |  picture_url longtext
         |)
      """.stripMargin
    )

    statement.execute(
      s"""
         |create table if not exists $GROUP_MEMBER_MEMBERSHIP_TABLE (
         |	group_member_id varchar(255),
         |  group_id varchar(255)
         |)
      """.stripMargin
    )

    try {
      statement.execute(
        s"""
           |ALTER TABLE $GROUP_MEMBER_MEMBERSHIP_TABLE
           |  ADD UNIQUE INDEX GROUP_MEMBER_RELATION (`group_member_id` ASC, `group_id` ASC);
        """.stripMargin
      )
    } catch {
      case _: Exception => // no-op
    }

    statement.close()

    debug(s"MySQL setup table time: ${elapsed().inMilliseconds.toString} ms")

    true
  }

  @throws[Exception]
  def close(): Boolean = {
    ds.close()
    true
  }

}
