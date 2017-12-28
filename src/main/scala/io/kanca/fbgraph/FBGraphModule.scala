package io.kanca.fbgraph

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import com.twitter.inject.annotations.Flag

object FBGraphModule extends TwitterModule {

  flag(name = "fbgraph.version", default = "2.11", help = "FB Graph API version.")

  @Singleton
  @Provides
  def providesGraph(
    @Flag("fbgraph.version") version: String,
  ): FBGraph = new FBGraphHttp(version)

}
