package io.kanca

import java.sql.Connection

import io.kanca.infra.MySQL
import org.scalatest.{FlatSpec, Matchers}

class MySQLSpec extends FlatSpec with Matchers {

  val connection = MySQL.getConnection

  "MySQL Spec" should "able to get connection" in {
    connection.isInstanceOf[Connection] shouldEqual true
  }

  it should "able to setup tables" in {
    MySQL.setupTables(connection) shouldEqual true
  }

}
