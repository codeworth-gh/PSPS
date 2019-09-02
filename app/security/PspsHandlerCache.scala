package security

import javax.inject.Inject
import javax.inject.Singleton
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.HandlerKey
import dataaccess.UsersDAO
import play.api.i18n.{Langs, MessagesApi}

/**
  * Handlers cache for the JFE Handlers.
  */

@Singleton
class PspsHandlerCache @Inject()(users:UsersDAO, langs:Langs, messagesApi:MessagesApi) extends HandlerCache {
  val defaultHandler: DeadboltHandler = new DeadboltHandler(users, langs, messagesApi)

  // Get the default handler.
  override def apply(): DeadboltHandler = defaultHandler

  // Get a named handler
  override def apply(handlerKey: HandlerKey): DeadboltHandler = defaultHandler
}
