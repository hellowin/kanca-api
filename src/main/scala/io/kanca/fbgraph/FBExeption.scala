package io.kanca.fbgraph

import play.api.libs.json._

case class FBUnhandledException(message: String, code: String, traceId: String, typ: String) extends Exception
case class FBOAuthException(message: String, code: String, traceId: String) extends Exception

protected trait FBException {

  private def parseExeption(json: JsValue): Exception = {
    val jsError: JsObject = (json \ "error").validate[JsObject].getOrElse(Json.obj())
    val typ: String = (jsError \ "type").validate[String].getOrElse("")
    val message: String = (jsError \ "message").validate[String].getOrElse("")
    val code: String = (jsError \ "code").validate[String].getOrElse("")
    val traceId: String = (jsError \ "fbtrace_id").validate[String].getOrElse("")

    typ match {
      case "OAuthException" => FBOAuthException(message, code, traceId)
      case _ => FBUnhandledException(message, code, traceId, typ)
    }
  }

  private def isException(json: JsValue): Boolean = {
    val error: JsResult[JsObject] = (json \ "error").validate[JsObject]
    error match {
      case s: JsSuccess[JsObject] => true
      case e: JsError => false
    }
  }

  protected def checkException(json: JsValue): Unit = {
    if (isException(json)) throw parseExeption(json)
  }

}
