package io.kanca

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import io.kanca.fbgraph.Graph

object GraphModule extends TwitterModule {

  @Singleton
  @Provides
  def providesGraph: Graph = new Graph

}
