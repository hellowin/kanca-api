package io.kanca.repository

import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.ZoneOffset

import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Duration, Stopwatch}
import io.kanca.core.FBGraphType._
import io.kanca.core.ResultType.GroupFeedResultSortType._
import io.kanca.core.ResultType.ResultSortOrder._
import io.kanca.core.ResultType._
import play.api.libs.json.{JsObject, Json}

import scala.collection.mutable.ListBuffer

class GroupFeedMySQL @Inject()(dataSource: DataSourceMySQL, groupCommentMySQL: GroupCommentMySQL, conf: ConfigurationMySQL) extends Logging {

  def insert(groupFeeds: List[GroupFeed]): Boolean = {

    if (groupFeeds.size < 1) return true

    val elapsed: () => Duration = Stopwatch.start()
    var comments: ListBuffer[(Comment, String, Option[String])] = ListBuffer[(Comment, String, Option[String])]()
    val itemsPerThread: Int = if ((groupFeeds.size / conf.numberOfThreadPerInject) == 0) 1 else groupFeeds.size / conf.numberOfThreadPerInject

    groupFeeds.grouped(itemsPerThread).toList.par.foreach(feedPool => {
      val connection: Connection = dataSource.getConnection

      val sql: String =
        """
          |insert into group_feed (
          |	id,
          | group_id,
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
          |	updated_time,
          | shares_count,
          | reactions,
          | reactions_summary
          |) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
          |ON DUPLICATE KEY UPDATE
          | group_id = values(group_id),
          | caption = values(caption),
          | created_time = values(created_time),
          | description = values(description),
          | from_name = values(from_name),
          | from_id = values(from_id),
          | link = values(link),
          | message = values(message),
          | message_tags = values(message_tags),
          | `name` = values(`name`),
          | permalink_url = values(permalink_url),
          | picture = values(picture),
          | status_type = values(status_type),
          | story = values(story),
          | `type` = values(`type`),
          | updated_time = values(updated_time),
          | shares_count = values(shares_count),
          | reactions = values(reactions),
          | reactions_summary = values(reactions_summary)
          |""".stripMargin
      val preparedStatement: PreparedStatement = connection.prepareStatement(sql)

      feedPool.foreach(feed => {
        val groupId = feed.id.split("_")(0)

        preparedStatement.setString(1, feed.id)
        preparedStatement.setString(2, groupId)
        preparedStatement.setString(3, feed.caption.orNull)
        preparedStatement.setTimestamp(4, Timestamp.valueOf(feed.createdTime))
        preparedStatement.setString(5, feed.description.orNull)
        preparedStatement.setString(6, feed.from.name)
        preparedStatement.setString(7, feed.from.id)
        preparedStatement.setString(8, feed.link.orNull)
        preparedStatement.setString(9, feed.message.orNull)
        preparedStatement.setString(10, Json.toJson(feed.messageTags.map { tag =>
          Json.obj(
            "id" -> tag.id,
            "name" -> tag.name,
            "type" -> tag.typ,
            "offset" -> tag.offset,
            "length" -> tag.length
          )
        }).toString())
        preparedStatement.setString(11, feed.name.orNull)
        preparedStatement.setString(12, feed.permalinkUrl)
        preparedStatement.setString(13, feed.picture.orNull)
        preparedStatement.setString(14, feed.statusType.orNull)
        preparedStatement.setString(15, feed.story.orNull)
        preparedStatement.setString(16, feed.typ)
        preparedStatement.setTimestamp(17, Timestamp.valueOf(feed.updatedTime))
        preparedStatement.setInt(18, feed.shares.count)
        preparedStatement.setString(19, Json.toJson(feed.reactions.data.map { rea =>
          Json.obj(
            "id" -> rea.id,
            "name" -> rea.name,
            "type" -> rea.typ
          )
        }).toString())
        preparedStatement.setString(20, Json.toJson(feed.reactions.data.groupBy(_.typ).map { case (key, list) =>
          Json.obj(
            "type" -> key,
            "count" -> list.size
          )
        }).toString())
        preparedStatement.addBatch()

        // add to comments for every feed comments and comment's comments
        comments ++= feed.comments.data.map(comment => (comment, feed.id, None))
        feed.comments.data.foreach(comment => {
          comments ++= comment.comments.data.map(comment2 => (comment2, feed.id, Some(comment.id)))
        })
      })

      preparedStatement.executeBatch()
      preparedStatement.close()

      connection.close()
    })

    debug(s"MySQL inject group feeds injected all feeds: ${elapsed().inMilliseconds} ms, total feeds: ${groupFeeds.size}, speed: ${elapsed().inMilliseconds / groupFeeds.size} ms per feed")
    if (comments.size > 0) {
      val groupId = groupFeeds.head.id.split("_")(0)
      groupCommentMySQL.insert(comments.toList, groupId)
      debug(s"MySQL group comments injected from all feeds: ${elapsed().inMilliseconds} ms, total feeds: ${comments.size}, speed: ${elapsed().inMilliseconds / groupFeeds.size} ms per comment")
    }

    true
  }

  def read(
    groupId: String,
    page: Int,
    limit: Int,
    sortBy: GroupFeedResultSortType,
    sortOrder: ResultSortOrder
  ): List[GroupFeedResult] = {
    val offset = limit * (page - 1)
    val connection: Connection = dataSource.getConnection

    val sqlSortType: String = sortBy match {
      case CREATED_TIME => "created_time"
      case UPDATED_TIME | _ => "updated_time"
    }

    val sqlSortOrder: String = sortOrder match {
      case ASC => "asc"
      case DESC | _ => "desc"
    }

    // fetch group feeds
    val statement = connection.createStatement()
    val rs: ResultSet = statement.executeQuery(
      s"""
         |select * from group_feed
         |  where group_id = "$groupId"
         |  order by $sqlSortType $sqlSortOrder
         |  limit $limit offset $offset
      """.stripMargin)
    val groupFeeds: ListBuffer[GroupFeedResult] = ListBuffer()
    while (rs.next()) {
      val groupFeed = GroupFeedResult(
        rs.getString("id"),
        Option(rs.getString("caption")),
        rs.getTimestamp("created_time").toLocalDateTime,
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
        rs.getTimestamp("updated_time").toLocalDateTime,
        Shares(
          rs.getInt("shares_count")
        ),
        Json.parse(rs.getString("reactions")).validate[List[JsObject]].getOrElse(List()).map(obj => Reaction(
          (obj \ "id").validate[String].get,
          (obj \ "name").validate[String].get,
          (obj \ "type").validate[String].get
        )),
        Json.parse(rs.getString("reactions_summary")).validate[List[JsObject]].getOrElse(List()).map(obj => ReactionSummary(
          (obj \ "type").validate[String].get,
          (obj \ "count").validate[Int].get
        )),
        List()
      )
      groupFeeds += groupFeed
    }

    // get List[String] of feed id
    val feedIds: List[String] = groupFeeds.map(feed => feed.id).toList

    // fetch comments only from selected feed ids
    val rsCom: ResultSet = statement.executeQuery(
      s"""
         |SELECT * FROM group_comment where feed_id in (${feedIds.map(id => s""""$id"""").mkString(",")})
      """.stripMargin)
    // our convention, internal repo comment enhanced to tuple (comment, feed_id, parent)
    val comments: ListBuffer[CommentResult] = ListBuffer()
    while (rsCom.next()) {
      val comment = CommentResult(
        rsCom.getString("id"),
        rsCom.getString("feed_id"),
        Option(rsCom.getString("parent_id")),
        From(
          rsCom.getString("from_name"),
          rsCom.getString("from_id")
        ),
        rsCom.getString("permalink_url"),
        rsCom.getString("message"),
        rsCom.getTimestamp("created_time").toLocalDateTime,
        Json.parse(rsCom.getString("reactions")).validate[List[JsObject]].getOrElse(List()).map(obj => Reaction(
          (obj \ "id").validate[String].get,
          (obj \ "name").validate[String].get,
          (obj \ "type").validate[String].get
        )),
        Json.parse(rsCom.getString("reactions_summary")).validate[List[JsObject]].getOrElse(List()).map(obj => ReactionSummary(
          (obj \ "type").validate[String].get,
          (obj \ "count").validate[Int].get
        )),
        List()
      )
      comments += comment
    }
    connection.close()

    // insert comment's comments to its parent
    // first, find the parents (grouped by its feed id for later use)
    val nestedComments: Map[String, List[CommentResult]] = comments.toList
      .filter(comment => comment.parentId.isDefined)
      .groupBy(_.feedId)
    // second, find the child
    val childComments: Map[String, List[CommentResult]] = comments.toList
      .groupBy(_.feedId)
    // third, inject childs to parents
    nestedComments.foreach {
      case (key, coms) => coms.foreach(com => {
        com.comments = childComments.getOrElse(com.id, List()).sortBy(_.createdTime.getNano).reverse
      })
    }

    // Then put comments to its feed
    groupFeeds.foreach(feed => {
      feed.comments = nestedComments.getOrElse(feed.id, List()).sortBy(_.createdTime.getNano).reverse
    })

    var finalResult: List[GroupFeedResult] = groupFeeds.toList

    sortBy match {
      case CREATED_TIME => finalResult = finalResult.sortBy(_.createdTime.toEpochSecond(ZoneOffset.UTC))
      case UPDATED_TIME | _ => finalResult = finalResult.sortBy(_.updatedTime.toEpochSecond(ZoneOffset.UTC))
    }

    sortOrder match {
      case ASC =>
      case DESC | _ => finalResult = finalResult.reverse
    }

    finalResult
  }

}
