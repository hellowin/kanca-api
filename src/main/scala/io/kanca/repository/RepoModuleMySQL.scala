package io.kanca.repository

import java.sql.Connection

import com.google.inject.{Inject, Provides, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import com.twitter.inject.annotations.Flag

object RepoModuleMySQL extends TwitterModule {

  flag(name = "repo.mysql.host", default = "localhost", help = "MySQL Host.")
  flag(name = "repo.mysql.port", default = "3306", help = "MySQL Port.")
  flag(name = "repo.mysql.database", default = "kanca_api", help = "MySQL Database.")
  flag[String](name = "repo.mysql.username", help = "MySQL Username.")
  flag[String](name = "repo.mysql.password", help = "MySQL Password.")
  flag(name = "repo.mysql.driver", default = "com.mysql.cj.jdbc.Driver", help = "MySQL Driver.")

  @Singleton
  @Provides
  def providesConnection(
    @Flag("repo.mysql.host") host: String,
    @Flag("repo.mysql.port") port: String,
    @Flag("repo.mysql.database") database: String,
    @Flag("repo.mysql.username") username: String,
    @Flag("repo.mysql.password") password: String,
    @Flag("repo.mysql.driver") driver: String,
  ): Connection = RepositoryMySQL.getConnection(host, port, database, username, password, driver)

  @Singleton
  @Provides
  def providesRepository(
    @Inject connection: Connection,
  ): Repository = new RepositoryMySQL(connection)

  override def singletonStartup(injector: Injector) {
    val connection: Connection = injector.instance[Connection]
    RepositoryMySQL.setupTables(connection)
  }

  override def singletonShutdown(injector: Injector) {
    val connection: Connection = injector.instance[Connection]
    connection.close()
  }

}
