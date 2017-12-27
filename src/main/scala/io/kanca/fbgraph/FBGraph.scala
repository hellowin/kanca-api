package io.kanca.fbgraph

import com.twitter.inject.Logging

abstract class FBGraph extends FBException with Logging {

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupFeed]

}