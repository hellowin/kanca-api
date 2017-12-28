package io.kanca.repository

import java.sql.{Connection, PreparedStatement, Timestamp}

import com.twitter.inject.Logging
import com.twitter.util.{Duration, Stopwatch}
import io.kanca.fbgraph._
import play.api.libs.json.Json

object GroupCommentMySQL extends Logging {

  // insert must list of (comment, feed id, parent id)
  def insert(connection: Connection, groupComments: List[(Comment, String, Option[String])], groupId: String): Boolean = {
    val elapsed: () => Duration = Stopwatch.start()

    val sql: String =
      """
        |insert into group_comment (
        |	id,
        | group_id,
        | feed_id,
        | parent_id,
        | created_time,
        | from_name,
        | from_id,
        | message,
        | permalink_url,
        | reactions,
        | reactions_summary
        |) values (?,?,?,?,?,?,?,?,?,?,?)
        |ON DUPLICATE KEY UPDATE
        | message = values(message),
        | reactions = values(reactions),
        | reactions_summary = values(reactions_summary)
        |""".stripMargin
    val preparedStatement: PreparedStatement = connection.prepareStatement(sql)

    groupComments.foreach { case (comment: Comment, feedId: String, parentId: Option[String]) => {
      preparedStatement.setString(1, comment.id)
      preparedStatement.setString(2, groupId)
      preparedStatement.setString(3, feedId)
      preparedStatement.setString(4, parentId.orNull)
      preparedStatement.setTimestamp(5, Timestamp.valueOf(comment.createdTime))
      preparedStatement.setString(6, comment.from.name)
      preparedStatement.setString(7, comment.from.id)
      preparedStatement.setString(8, comment.message)
      preparedStatement.setString(9, comment.permalinkUrl)
      preparedStatement.setString(10, Json.toJson(comment.reactions.data.map { rea =>
        Json.obj(
          "id" -> rea.id,
          "name" -> rea.name,
          "type" -> rea.typ
        )
      }).toString())
      preparedStatement.setString(11, Json.toJson(comment.reactions.data.groupBy(_.typ).map { case (key, list) =>
        Json.obj(
          "type" -> key,
          "count" -> list.size
        )
      }).toString())
      preparedStatement.addBatch()
    }}
    debug(s"MySQL inject comments prepared batch: ${elapsed().inMilliseconds} ms")
    preparedStatement.executeBatch()
    preparedStatement.close()
    debug(s"MySQL inject comments injected: ${elapsed().inMilliseconds} ms")

    true
  }

}
