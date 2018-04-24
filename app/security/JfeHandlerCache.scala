package security

import javax.inject.Inject
import javax.inject.Singleton
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.HandlerKey
import dataaccess.UsersDAO

/**
  * Handlers cache for the JFE Handlers.
  */

@Singleton
class JfeHandlerCache @Inject()(users:UsersDAO) extends HandlerCache {
  val defaultHandler: DeadboltHandler = new DeadboltHandler(users)

  // Get the default handler.
  override def apply(): DeadboltHandler = defaultHandler

  // Get a named handler
  override def apply(handlerKey: HandlerKey): DeadboltHandler = defaultHandler
}
