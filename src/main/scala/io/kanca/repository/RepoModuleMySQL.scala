package io.kanca.repository

import com.google.inject.{Inject, Provides, Singleton}
import com.twitter.inject.annotations.Flag
import com.twitter.inject.{Injector, TwitterModule}

object RepoModuleMySQL extends TwitterModule {

  flag(name = "repo.mysql.host", default = "localhost", help = "MySQL Host.")
  flag(name = "repo.mysql.port", default = "3306", help = "MySQL Port.")
  flag(name = "repo.mysql.database", default = "kanca_api", help = "MySQL Database.")
  flag[String](name = "repo.mysql.username", help = "MySQL Username.")
  flag[String](name = "repo.mysql.password", help = "MySQL Password.")
  flag(name = "repo.mysql.driver", default = "com.mysql.cj.jdbc.Driver", help = "MySQL Driver.")
  flag[Int](name = "repo.readLimit", default = 100, help = "Read limit per page.")

  @Singleton
  @Provides
  def providesDataSource(
    @Flag("repo.mysql.host") host: String,
    @Flag("repo.mysql.port") port: String,
    @Flag("repo.mysql.database") database: String,
    @Flag("repo.mysql.username") username: String,
    @Flag("repo.mysql.password") password: String,
    @Flag("repo.mysql.driver") driver: String,
  ): DataSource = new DataSourceMySQL(host, port, database, username, password, driver)

  @Singleton
  @Provides
  def providesGroupCommentMySQL(
    @Inject dataSource: DataSource,
  ): GroupCommentMySQL = new GroupCommentMySQL(dataSource)

  @Singleton
  @Provides
  def providesGroupFeedMySQL(
    @Inject dataSource: DataSource,
    @Inject groupCommentMySQL: GroupCommentMySQL,
  ): GroupFeedMySQL = new GroupFeedMySQL(dataSource, groupCommentMySQL)

  @Singleton
  @Provides
  def providesRepository(
    @Inject groupFeedMySQL: GroupFeedMySQL,
    @Flag("repo.readLimit") readLimit: Int,
  ): Repository = new RepositoryMySQL(groupFeedMySQL, readLimit)

  override def singletonStartup(injector: Injector) {
    val dataSource: DataSource = injector.instance[DataSource]
    dataSource.setup()
  }

  override def singletonShutdown(injector: Injector) {
    val dataSource: DataSource = injector.instance[DataSource]
    dataSource.close()
  }

}
