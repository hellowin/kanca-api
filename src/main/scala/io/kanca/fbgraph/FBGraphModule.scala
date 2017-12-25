package io.kanca.fbgraph

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import com.twitter.inject.annotations.Flag

object FBGraphModule extends TwitterModule {

  flag(name = "fbgraph.version", default = "2.11", help = "FB Graph API version.")
  flag[Int](name = "fbgraph.defaultPageLimit", default = 10, help = "Default page limit when fetching Graph API pagination.")
  flag(name = "fbgraph.defaultRequestLimit", default = "100", help = "Default request limit single fetching Graph API.")

  @Singleton
  @Provides
  def providesGraph(
    @Flag("fbgraph.version") version: String,
    @Flag("fbgraph.defaultPageLimit") defaultPageLimit: Int,
    @Flag("fbgraph.defaultRequestLimit") defaultRequestLimit: String,
  ): FBGraph = new FBGraphHttp(version, defaultPageLimit, defaultRequestLimit)

}
