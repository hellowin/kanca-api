package io.kanca.repository

import java.sql.ResultSet
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import com.google.inject.Inject
import com.twitter.inject.Logging
import io.kanca.core.MetricType.{ActivityByDate, ActivityByTime}

import scala.collection.mutable.ListBuffer

class MetricsMySQL @Inject()(dataSource: DataSourceMySQL, conf: ConfigurationMySQL) extends Logging {

  val DATE_FORMATTER = "yyyy-MM-dd"
  val TIME_FORMATTER = "HH:mm:ss"

  def readActivitiesByDate(groupId: String, dateStart: LocalDate, dateEnd: LocalDate): List[ActivityByDate] = {
    val connection = dataSource.getConnection
    val stmt = connection.createStatement()
    val rs: ResultSet = stmt.executeQuery(
      s"""
         |SELECT feed.count as feeds_count, comment.count as comments_count, feed.date as date
         |FROM
         |(
         |	SELECT count(*) as count, date(created_time) as date
         |    FROM group_feed
         |	WHERE group_id = '$groupId'
         |	GROUP BY date
         |) as feed
         |LEFT OUTER JOIN
         |(
         |	SELECT count(*) as count, date(created_time) as date
         |	FROM group_comment
         |	WHERE group_id = '$groupId'
         |	GROUP BY date
         |) as comment
         |ON feed.date = comment.date
         |
         |UNION
         |
         |SELECT feed.count as feed_count, comment.count as comment_count, feed.date as date
         |FROM
         |(
         |	SELECT count(*) as count, date(created_time) as date
         |    FROM group_feed
         |	WHERE group_id = '$groupId'
         |	GROUP BY date
         |) as feed
         |LEFT JOIN
         |(
         |	SELECT count(*) as count, date(created_time) as date
         |	FROM group_comment
         |	WHERE group_id = '$groupId'
         |	GROUP BY date
         |) as comment
         |ON feed.date = comment.date
         |
         |WHERE feed.date BETWEEN "${dateStart format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         | AND "${dateEnd format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |ORDER BY date
      """.stripMargin
    )

    val results: ListBuffer[ActivityByDate] = ListBuffer()
    while (rs.next()) {
      val activity = ActivityByDate(
        Option(rs getInt "feeds_count") getOrElse 0,
        Option(rs getInt "comments_count") getOrElse 0,
        LocalDate parse(rs.getString("date"), DateTimeFormatter ofPattern DATE_FORMATTER)
      )
      results += activity
    }

    results.toList
  }

  def readActivitiesByTime(groupId: String, dateStart: LocalDate, dateEnd: LocalDate): List[ActivityByTime] = {
    val connection = dataSource.getConnection
    val stmt = connection.createStatement()
    val rs: ResultSet = stmt.executeQuery(
      s"""
         |SELECT feed.count as feeds_count, comment.count as comments_count, feed.time as time
         |FROM
         |(
         |	SELECT count(*) as count, hour(created_time) as time
         |    FROM group_feed
         |	WHERE group_id = '$groupId'
         |    AND
         |    date(created_time) BETWEEN "${dateStart format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |    AND "${dateEnd format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |	GROUP BY time
         |) as feed
         |LEFT JOIN
         |(
         |	SELECT count(*) as count, hour(created_time) as time
         |	FROM group_comment
         |	WHERE group_id = '$groupId'
         |    AND
         |    date(created_time) BETWEEN "${dateStart format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |    AND "${dateEnd format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |	GROUP BY time
         |) as comment
         |ON feed.time = comment.time
         |
         |UNION
         |
         |SELECT feed.count as feed_count, comment.count as comment_count, feed.time as time
         |FROM
         |(
         |	SELECT count(*) as count, hour(created_time) as time
         |    FROM group_feed
         |	WHERE group_id = '$groupId'
         |    AND
         |    date(created_time) BETWEEN "${dateStart format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |    AND "${dateEnd format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |	GROUP BY time
         |) as feed
         |LEFT JOIN
         |(
         |	SELECT count(*) as count, hour(created_time) as time
         |	FROM group_comment
         |	WHERE group_id = '$groupId'
         |    AND
         |    date(created_time) BETWEEN "${dateStart format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |    AND "${dateEnd format (DateTimeFormatter ofPattern DATE_FORMATTER)}"
         |	GROUP BY time
         |) as comment
         |ON feed.time = comment.time
         |
         |ORDER BY time
      """.stripMargin
    )

    val results: ListBuffer[ActivityByTime] = ListBuffer()
    while (rs.next()) {
      val activity = ActivityByTime(
        Option(rs getInt "feeds_count") getOrElse 0,
        Option(rs getInt "comments_count") getOrElse 0,
        LocalTime parse(rs.getString("time"), DateTimeFormatter ofPattern TIME_FORMATTER)
      )
      results += activity
    }

    results.toList
  }

}
