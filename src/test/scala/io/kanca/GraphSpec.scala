package io.kanca

import org.scalatest._
import io.kanca.fbgraph.{Graph, GroupFeed}

class GraphSpec extends FlatSpec with Matchers {

  val userToken = "EAAFhcjTi6hIBAHb3shaLzFWRZAcQadDnfIkJHGCXsZBVfZCfZCUOhD7IXMkg7ZAkrLoWOY7m5H8ha3ZBLhgr4KVDZAEZAaQlDnf64NIgnVjGtISYfCFLg5hYITsCVpAYsEKZBzcSNJDL0rK7mVG70wRQnhKiLQZCywJFc99Q7pp0Ky0cqJ4HbEdGRIzUz3MQmCoxuxIhnM4IVusAZDZD"
  val groupId = "1920036621597031"

  "Group fetcher" should "able to get feeds by default arguments" in {
    val feeds: List[GroupFeed] = Graph.getGroupFeeds(userToken, groupId)
    feeds.size should be <= 10*100
    feeds.size should be > 100
  }

  it should "able to get feeds by page limit arguments" in {
    val feeds: List[GroupFeed] = Graph.getGroupFeeds(userToken, groupId, 2)
    feeds should have size 2*100
  }

}
