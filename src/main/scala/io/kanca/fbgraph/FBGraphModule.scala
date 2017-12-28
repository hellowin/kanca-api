package io.kanca.fbgraph

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import com.twitter.inject.annotations.Flag

object FBGraphModule extends TwitterModule {

  flag(name = "fbgraph.version", default = "2.11", help = "FB Graph API version.")
  flag[Int](name = "fbgraph.connectionTimeout", default = 2000, help = "GraphAPI HTTP request connection timeout.")
  flag[Int](name = "fbgraph.readTimeout", default = 10000, help = "GraphAPI HTTP request read timeout.")

  @Singleton
  @Provides
  def providesGraph(
    @Flag("fbgraph.version") version: String,
    @Flag("fbgraph.connectionTimeout") connectionTimeout: Int,
    @Flag("fbgraph.readTimeout") readTimeout: Int,
  ): FBGraph = new FBGraphHttp(version, connectionTimeout, readTimeout)

}
