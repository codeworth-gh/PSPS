package controllers
import models.User
import play.api.libs.typedmap.TypedKey

object Attrs {
  val User: TypedKey[User] = TypedKey.apply[User]("user")
}
