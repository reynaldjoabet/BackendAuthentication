package domain
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
case class User(id: Long, username: String, roles: Set[String])

object User {
  implicit val userEncoder: Encoder[domain.User] = deriveEncoder[User]
}
