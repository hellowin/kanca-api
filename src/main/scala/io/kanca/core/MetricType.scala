package io.kanca.core

import java.time.{DayOfWeek, LocalDate, LocalTime}

object MetricType {

  case class ActivityByDate(feedsCount: Int, commentsCount: Int, date: LocalDate)

  case class ActivityByTime(feedsCount: Int, commentsCount: Int, time: LocalTime)

  case class ActivityByDayOfWeek(feedsCount: Int, commentsCount: Int, dayOfWeek: DayOfWeek)

}
