package io.kanca.fbgraph

import com.twitter.inject.Logging

abstract class FBGraph(defaultPageLimit: Int) extends FBException with Logging {

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = defaultPageLimit): List[GroupFeed]

}