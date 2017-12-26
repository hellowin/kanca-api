package io.kanca.fbgraph

import java.net.URL

import play.api.libs.json.JsObject

import scala.io.{Codec, Source}

class FBGraphMock(defaultPageLimit: Int) extends FBGraph(defaultPageLimit) {

  private def getResultArrayFromFile[T](filePrefix: String, parser: JsObject => T, pageLimit: Int, results: List[T] = List()): List[T] = {
    try {
      val path = s"/fixture/${filePrefix}_$pageLimit.json"
      debug(s"Getting mock JSON from file $path")
      val resource: URL = getClass.getResource(path)
      val jsonString: String = Source.fromURL(resource)(Codec.UTF8).mkString
      val (result, next) = parseStringResultArray[T](jsonString, parser)
      val newResults = results ::: result
      val nextPage = pageLimit - 1
      if (nextPage > 0) {
        getResultArrayFromFile[T](filePrefix, parser, nextPage, newResults)
      } else {
        newResults
      }
    } catch {
      case e: Exception => getResultArrayFromFile[T](filePrefix, parser, pageLimit - 1, results)
    }
  }

  override def getGroupFeeds(token: String, groupId: String, pageLimit: Int): List[GroupFeed] =
    getResultArrayFromFile[GroupFeed]("group_feed", GroupFeed.parse, pageLimit)

}
