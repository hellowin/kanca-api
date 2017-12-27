package io.kanca.fbgraph

import java.net.URL

import play.api.libs.json.JsObject

import scala.io.{Codec, Source}

class FBGraphMock(defaultPageLimit: Int) extends FBGraph(defaultPageLimit) {

  private def getResultArrayFromFile[T](filePrefix: String, parser: JsObject => T, pageLimit: Int, results: List[T] = List()): FBListResult[T] = {
    try {
      val path = s"/fixture/${filePrefix}_$pageLimit.json"
      debug(s"Getting mock JSON from file $path")
      val resource: URL = getClass.getResource(path)
      val jsonString: String = Source.fromURL(resource)(Codec.UTF8).mkString
      val listResult: FBListResult[T] = FBListResult.parse[T](jsonString, parser)
      val newResults = results ::: listResult.data
      val nextPage = pageLimit - 1
      if (nextPage > 0) {
        getResultArrayFromFile[T](filePrefix, parser, nextPage, newResults)
      } else {
        FBListResult(newResults, None)
      }
    } catch {
      case e: Exception => getResultArrayFromFile[T](filePrefix, parser, pageLimit - 1, results)
    }
  }

  override def getGroupFeeds(token: String, groupId: String, pageLimit: Int): FBListResult[GroupFeed] =
    getResultArrayFromFile[GroupFeed]("group_feed", GroupFeed.parse, pageLimit)

}