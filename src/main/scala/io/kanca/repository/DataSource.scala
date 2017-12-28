package io.kanca.repository

import java.sql.Connection

trait DataSource {

  def getConnection: Connection

  def setup(): Boolean

  def close(): Boolean

}
