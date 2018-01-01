package io.kanca.repository

import com.google.inject.{Inject, Provides, Singleton}
import com.twitter.inject.annotations.Flag
import com.twitter.inject.{Injector, TwitterModule}
import io.kanca.core.Repository

object RepoModuleMySQL extends TwitterModule {

  flag(name = "repo.mysql.host", default = "localhost", help = "MySQL Host.")
  flag(name = "repo.mysql.port", default = "3306", help = "MySQL Port.")
  flag(name = "repo.mysql.database", default = "kanca_api", help = "MySQL Database.")
  flag[String](name = "repo.mysql.username", help = "MySQL Username.")
  flag[String](name = "repo.mysql.password", help = "MySQL Password.")
  flag(name = "repo.mysql.driver", default = "com.mysql.cj.jdbc.Driver", help = "MySQL Driver.")
  flag[Int](name = "repo.mysql.connectionTimeout", default = 30000, help = "Timeout when things too long waiting get connection from connection pool.")
  flag[Int](name = "repo.mysql.connectionPoolSize", default = 10, help = "Maximum connections per pool.")
  flag[Int](name = "repo.mysql.numberOfThreadPerInject", default = 7, help = "Number or thread uses when performing inject.")
  flag[Boolean](name = "repo.mysql.useSSL", default = false, help = "Will the connection use SSL?")
  flag[Int](name = "repo.readLimit", default = 100, help = "Read limit per page.")

  @Singleton
  @Provides
  def providesMySQLConfiguration(
    @Flag("repo.mysql.host") host: String,
    @Flag("repo.mysql.port") port: String,
    @Flag("repo.mysql.database") database: String,
    @Flag("repo.mysql.username") username: String,
    @Flag("repo.mysql.password") password: String,
    @Flag("repo.mysql.driver") driver: String,
    @Flag("repo.readLimit") readLimit: Int,
    @Flag("repo.mysql.connectionTimeout") connectionTimeout: Int,
    @Flag("repo.mysql.numberOfThreadPerInject") numberOfThreadPerInject: Int,
    @Flag("repo.mysql.connectionPoolSize") connectionPoolSize: Int,
    @Flag("repo.mysql.useSSL") useSSL: Boolean,
  ): ConfigurationMySQL = ConfigurationMySQL(host, port, database, username, password, driver, readLimit, connectionTimeout, numberOfThreadPerInject, connectionPoolSize, useSSL)

  @Singleton
  @Provides
  def providesRepository(
    @Inject conf: ConfigurationMySQL,
    @Inject dataSource: DataSourceMySQL,
    @Inject groupFeedMySQL: GroupFeedMySQL,
    @Inject groupMemberMySQL: GroupMemberMySQL,
    @Inject metricsMySQL: MetricsMySQL,
  ): Repository = new RepositoryMySQL(conf, dataSource, groupFeedMySQL, groupMemberMySQL, metricsMySQL)

  override def singletonStartup(injector: Injector) {
    val repository: Repository = injector.instance[Repository]
    repository.initialize()
  }

  override def singletonShutdown(injector: Injector) {
    val repository: Repository = injector.instance[Repository]
    repository.shutdown()
  }

}
