package domain
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
case class User(id: Long, username: String, roles: Set[String])

object User {
  implicit val userCodec: Codec[User] = deriveCodec[User]
}
