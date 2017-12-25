package io.kanca

import com.google.inject.Inject
import io.kanca.fbgraph.GroupFeed

class GraphService @Inject() (graph: Graph) {
  def getGroupFeeds(token: String, groupId: String): List[GroupFeed] = graph.getGroupFeeds(token, groupId)
}
