package io.kanca.repository

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import io.kanca.fbgraph.{From, GroupFeed, MessageTag}
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.collection.mutable.ListBuffer

object GroupFeedRepo {

  val READ_LIMIT = sys.env("READ_LIMIT")

  def insert(connection: Connection, groupFeeds: List[GroupFeed]): Boolean = {
    val sql: String =
      """
        |insert into group_feed (
        |	id,
        | caption,
        |	created_time,
        |	description,
        |	from_name,
        |	from_id,
        |	link,
        |	message,
        |	message_tags,
        |	`name`,
        |	permalink_url,
        |	picture,
        |	status_type,
        |	story,
        |	`type`,
        |	updated_time
        |) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        |ON DUPLICATE KEY UPDATE
        | caption = values(caption),
        | description = values(description)
        |""".stripMargin
    val preparedStatement: PreparedStatement = connection.prepareStatement(sql)

    groupFeeds.foreach(feed => {
      preparedStatement.setString(1, feed.id)
      preparedStatement.setString(2, feed.caption.orNull)
      preparedStatement.setString(3, feed.createdTime.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + feed.createdTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
      preparedStatement.setString(4, feed.description.orNull)
      preparedStatement.setString(5, feed.from.name)
      preparedStatement.setString(6, feed.from.id)
      preparedStatement.setString(7, feed.link.orNull)
      preparedStatement.setString(8, feed.message.orNull)
      preparedStatement.setString(9, Json.toJson(feed.messageTags.map { tag =>
        Json.obj(
          "id" -> tag.id,
          "name" -> tag.name,
          "type" -> tag.typ,
          "offset" -> tag.offset,
          "length" -> tag.length
        )
      }).toString())
      preparedStatement.setString(10, feed.name.orNull)
      preparedStatement.setString(11, feed.permalinkUrl)
      preparedStatement.setString(12, feed.picture.orNull)
      preparedStatement.setString(13, feed.statusType.orNull)
      preparedStatement.setString(14, feed.story.orNull)
      preparedStatement.setString(15, feed.typ)
      preparedStatement.setString(16, feed.updatedTime.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + feed.updatedTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
      preparedStatement.addBatch()
    })
    preparedStatement.execute()
    preparedStatement.close()

    true
  }

  def read(connection: Connection): List[GroupFeed] = {
    val statement = connection.createStatement()
    val rs: ResultSet = statement.executeQuery(
      s"""
        |select * from group_feed limit $READ_LIMIT;
      """.stripMargin)
    val groupFeeds: ListBuffer[GroupFeed] = ListBuffer()
    while (rs.next()) {
      val groupFeed = GroupFeed(
        rs.getString("id"),
        Option(rs.getString("caption")),
        LocalDateTime.ofInstant(rs.getTimestamp("created_time").toInstant, ZoneId.of("UTC")),
        Option(rs.getString("description")),
        From(
          rs.getString("from_name"),
          rs.getString("from_id")
        ),
        Option(rs.getString("link")),
        Option(rs.getString("message")),
        Json.parse(rs.getString("message_tags")).validate[List[JsObject]].getOrElse(List()).map(obj => {
          MessageTag(
            (obj \ "id").validate[String].get,
            (obj \ "name").validate[String].get,
            (obj \ "type").validate[String].get,
            (obj \ "length").validate[Int].get,
            (obj \ "offset").validate[Int].get
          )
        }),
        Option(rs.getString("name")),
        rs.getString("permalink_url"),
        Option(rs.getString("picture")),
        Option(rs.getString("status_type")),
        Option(rs.getString("story")),
        rs.getString("type"),
        LocalDateTime.ofInstant(rs.getTimestamp("updated_time").toInstant, ZoneId.of("UTC"))
      )
      groupFeeds += groupFeed
    }

    groupFeeds.toList
  }

}
