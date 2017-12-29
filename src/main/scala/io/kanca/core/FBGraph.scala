package io.kanca.core

import com.twitter.inject.Logging
import io.kanca.fbgraph.{FBListResult, GroupFeed}

abstract class FBGraph extends Logging {

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupFeed]

}