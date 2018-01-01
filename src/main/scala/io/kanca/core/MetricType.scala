package io.kanca.core

import java.time.{LocalDate, LocalTime}

object MetricType {

  case class ActivityByDate(feedsCount: Int, commentsCount: Int, date: LocalDate)

  case class ActivityByTime(feedsCount: Int, commentsCount: Int, time: LocalTime)

}
