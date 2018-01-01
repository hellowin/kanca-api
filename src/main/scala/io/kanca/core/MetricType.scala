package io.kanca.core

import java.time.LocalDate

object MetricType {

  case class ActivityByDate(feedsCount: Int, commentsCount: Int, date: LocalDate)

}
