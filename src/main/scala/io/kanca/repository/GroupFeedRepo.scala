package io.kanca.repository

import java.sql.{Connection, PreparedStatement}
import java.time.format.DateTimeFormatter

import io.kanca.fbgraph.GroupFeed
import play.api.libs.json.Json

object GroupFeedRepo {

  def insertGroupFeeds(connection: Connection, groupFeeds: List[GroupFeed]): Boolean = {
    val sql: String =
      """insert into group_feed (
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

}
