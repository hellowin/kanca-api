package io.kanca.fbgraph

import com.twitter.inject.Logging
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

abstract class FBGraph(defaultPageLimit: Int) extends FBException with Logging {

  def getGroupFeeds(token: String, groupId: String, pageLimit: Int = defaultPageLimit): List[GroupFeed]

  protected def parseStringResultArray[T](string: String, parser: JsObject => T): (List[T], Option[String]) = {
    val rawJson: JsValue = Json.parse(string)

    // handle error
    checkException(rawJson)

    val data: List[JsObject] = (rawJson \ "data").validate[JsArray].getOrElse(Json.arr()).as[List[JsObject]]
    val next: Option[String] = (rawJson \ "paging" \ "next").validate[String].asOpt

    val result: List[T] = data.map(parser)

    (result, next)
  }

}