package io.kanca

import java.time.{LocalDateTime, Month}
import java.time.format.DateTimeFormatter

import org.scalatest._

class TimeTest extends FlatSpec with Matchers {

  val DATE_TIME_STRING = "2017-12-22T04:06:57+0000"
  val TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ssZ"

  s"DateTime Java Test" should "able to parse date time from string" in {
    val dateTime: LocalDateTime = LocalDateTime.parse(DATE_TIME_STRING, DateTimeFormatter.ofPattern(TIME_FORMATTER))
    dateTime.getYear shouldEqual 2017
    dateTime.getMonth shouldEqual Month.DECEMBER
    dateTime.getDayOfMonth shouldEqual 22
    dateTime.getHour shouldEqual 4
    dateTime.getMinute shouldEqual 6
    dateTime.getSecond shouldEqual 57
  }

}
