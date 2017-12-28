package io.kanca.repository

case class MySQLConfiguration(
  host: String,
  port: String,
  database: String,
  username: String,
  password: String,
  driver: String,
  readLimit: Int,
  connectionTimeout: Long,
  numberOfThreadPerInject: Int,
  connectionPoolSize: Int,
)
