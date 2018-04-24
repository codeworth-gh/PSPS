package security

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

/**
  * Created by michael on 13/3/17.
  */
class DeadboltModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[HandlerCache].to[JfeHandlerCache]
  )
}
