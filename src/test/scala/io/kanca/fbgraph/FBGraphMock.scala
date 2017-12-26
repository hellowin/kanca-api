package io.kanca.fbgraph

import java.net.URL

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

import scala.io.{Codec, Source}

class FBGraphMock extends FBGraph(10) {

  override def getGroupFeeds(token: String, groupId: String, pageLimit: Int): List[GroupFeed] = {
    val resource: URL = getClass.getResource("/fixture/group_feed_1.json")
    val jsonString: String = Source.fromURL(resource)(Codec.UTF8).mkString
    val rawJson: JsValue = Json.parse(jsonString)
    val data: List[JsObject] = (rawJson \ "data").validate[JsArray].getOrElse(Json.arr()).as[List[JsObject]]
    data.map(GroupFeed.parse)
  }

}
