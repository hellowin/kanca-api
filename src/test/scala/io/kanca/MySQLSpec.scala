package io.kanca

import java.sql.Connection

import io.kanca.infra.MySQL
import org.scalatest.{FlatSpec, Matchers}

class MySQLSpec extends FlatSpec with Matchers {

  "MySQL Spec" should "able to get connection" in {
    val connection = MySQL.getConnection
    connection.isInstanceOf[Connection] shouldEqual true
  }

}
