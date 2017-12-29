package io.kanca.fbgraph

import java.net.URL

import io.kanca.core.FBGraph
import io.kanca.core.FBGraphType.{FBListResult, GroupFeed}
import play.api.libs.json.JsObject

import scala.io.{Codec, Source}

class FBGraphMock extends FBGraph {

  private def getResultArrayFromFile[T](filePrefix: String, parser: JsObject => T, pageLimit: Int, results: List[T] = List()): FBListResult[T] = {
    try {
      val path = s"/fixture/${filePrefix}_$pageLimit.json"
      debug(s"Getting mock JSON from file $path")
      val resource: URL = getClass.getResource(path)
      val jsonString: String = Source.fromURL(resource)(Codec.UTF8).mkString
      val listResult: FBListResult[T] = FBListResultParser.parse[T](jsonString, parser)
      val newResults = results ::: listResult.data
      val nextPage = pageLimit - 1
      if (nextPage > 0) {
        getResultArrayFromFile[T](filePrefix, parser, nextPage, newResults)
      } else {
        FBListResult(newResults, None)
      }
    } catch {
      case _: NullPointerException =>
        if (pageLimit < 1) return FBListResult(results, None)
        getResultArrayFromFile[T](filePrefix, parser, pageLimit - 1, results)
    }
  }

  override def getGroupFeeds(token: String, groupId: String, pageLimit: Int, requestLimit: Int): FBListResult[GroupFeed] =
    getResultArrayFromFile[GroupFeed]("group_feed", GroupFeedParser.parse, pageLimit)

}
