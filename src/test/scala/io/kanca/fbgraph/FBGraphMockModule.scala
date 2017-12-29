package io.kanca.fbgraph

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import com.twitter.inject.annotations.Flag
import io.kanca.core.FBGraph

object FBGraphMockModule extends TwitterModule {

  flag[Int](name = "fbgraph.defaultPageLimit", default = 10, help = "Default page limit when fetching Graph API pagination.")

  @Singleton
  @Provides
  def providesGraph(
    @Flag("fbgraph.defaultPageLimit") defaultPageLimit: Int,
  ): FBGraph = new FBGraphMock

}
