package io.kanca.fbgraph

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule

object FBGraphModule extends TwitterModule {

  @Singleton
  @Provides
  def providesGraph: FBGraph = new FBGraphHttp

}
