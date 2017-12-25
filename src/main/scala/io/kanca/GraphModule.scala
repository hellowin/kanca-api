package io.kanca

import com.twitter.inject.{InjectorModule, TwitterModule}
import io.kanca.fbgraph.FacebookGraph

object GraphModule extends TwitterModule {

  override val modules = Seq(InjectorModule)

  override def configure() {
    bindSingleton[Graph].to[FacebookGraph]
  }

}
