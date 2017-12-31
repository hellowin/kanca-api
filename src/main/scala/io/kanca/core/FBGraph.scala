package io.kanca.core

import com.twitter.inject.Logging
import io.kanca.core.FBGraphType.{FBListResult, GroupFeed, GroupMember}

abstract class FBGraph extends Logging {

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupFeed]

  def getGroupMembers(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupMember]

}