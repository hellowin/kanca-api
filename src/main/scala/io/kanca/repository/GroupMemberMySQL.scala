package io.kanca.repository

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.google.inject.Inject
import com.twitter.inject.Logging
import com.twitter.util.{Duration, Stopwatch}
import io.kanca.core.FBGraphType._
import io.kanca.core.ResultType.ResultSortOrder._
import io.kanca.core.ResultType.ResultSortType._

import scala.collection.mutable.ListBuffer

class GroupMemberMySQL @Inject()(dataSource: DataSourceMySQL, groupCommentMySQL: GroupCommentMySQL, conf: ConfigurationMySQL) extends Logging {

  def insert(groupId: String, members: List[GroupMember]): Boolean = {

    if (members.size < 1) return true

    val elapsed: () => Duration = Stopwatch.start()
    val itemsPerThread: Int = if ((members.size / conf.numberOfThreadPerInject) == 0) 1 else members.size / conf.numberOfThreadPerInject

    members.grouped(itemsPerThread).toList.par.foreach(memberPool => {
      val connection: Connection = dataSource.getConnection

      // add to group member table
      val sql: String =
        """
          |insert into group_member (
          |	id,
          | name,
          | picture_url
          |) values (?,?,?)
          |ON DUPLICATE KEY UPDATE
          | name = values(name),
          | picture_url = values(picture_url)
          |""".stripMargin
      val preparedStatement: PreparedStatement = connection.prepareStatement(sql)

      // add to group member membership table
      val sql2: String =
        """
          |insert into group_member_membership (
          |	group_member_id,
          | group_id
          |) values (?,?)
          |ON DUPLICATE KEY UPDATE
          | group_id = values(group_id)
          |""".stripMargin
      val preparedStatement2: PreparedStatement = connection.prepareStatement(sql2)

      memberPool.foreach(member => {
        preparedStatement.setString(1, member.id)
        preparedStatement.setString(2, member.name)
        preparedStatement.setString(3, member.pictureUrl)
        preparedStatement.addBatch()

        preparedStatement2.setString(1, member.id)
        preparedStatement2.setString(2, groupId)
        preparedStatement2.addBatch()
      })

      preparedStatement.executeBatch()
      preparedStatement.close()
      preparedStatement2.executeBatch()
      preparedStatement2.close()

      connection.close()
    })

    debug(s"MySQL inject group members injected all members: ${elapsed().inMilliseconds} ms, total members: ${members.size}, speed: ${elapsed().inMilliseconds / members.size} ms per member")

    true
  }

  def read(
    groupId: String,
    page: Int,
    limit: Int,
    sortBy: ResultSortType,
    sortOrder: ResultSortOrder
  ): List[GroupMember] = {
    val offset = limit * (page - 1)
    val connection: Connection = dataSource.getConnection

    val sqlSortType: String = sortBy match {
      case _ => "name"
    }

    val sqlSortOrder: String = sortOrder match {
      case ASC => "asc"
      case DESC | _ => "desc"
    }

    // fetch group feeds
    val statement = connection.createStatement()
    val rs: ResultSet = statement.executeQuery(
      s"""
         |select * from group_member
         |  join group_member_membership on group_member.id = group_member_membership.group_member_id
         |  where group_id = "$groupId"
         |  order by $sqlSortType $sqlSortOrder
         |  limit $limit offset $offset
      """.stripMargin)
    val members: ListBuffer[GroupMember] = ListBuffer()
    while (rs.next()) {
      val member = GroupMember(
        rs.getString("id"),
        rs.getString("name"),
        rs.getString("picture_url")
      )
      members += member
    }

    var finalResult: List[GroupMember] = members.toList

    sortBy match {
      case _ => finalResult = finalResult.sortBy(_.name)
    }

    sortOrder match {
      case ASC =>
      case DESC | _ => finalResult = finalResult.reverse
    }

    finalResult
  }

}
