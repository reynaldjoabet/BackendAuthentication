package repositories
import domain._
import cats.effect.kernel
import skunk.data.Type
import cats.effect._
import skunk._
import skunk.syntax.all._
//import skunk.implicits._
import skunk.codec.all._
import cats.syntax.all._
trait RecoveryTokenRepository[F[_]] {

  def getToken(email: String): F[Option[String]]
  def checkToken(email: String, token: String): F[Boolean]

}

class RecoveryTokenRepositoryLive[F[_]: Concurrent](
    postgres: Resource[F, Session[F]]
) extends RecoveryTokenRepository[F] {
  override def getToken(email: String): F[Option[String]] = {
    val query = sql"""
      SELECT token FROM recovery_tokens where email =$text
      """.query(text)

    postgres.use(_.prepare(query).flatMap(_.option(email)))
  }

  override def checkToken(email: String, token: String): F[Boolean] = {
    val query = sql"""
    SELECT * FROM recovery_tokens WHERE email=$text AND token=$text 
    """
      .query(text ~ text ~ int8)
      .map { case (email ~ token ~ expiration) =>
        PasswordRecoveryToken(email, token, expiration)
      }

    postgres.use(
      _.prepare(query).flatMap(_.option(email ~ token).map(_.isDefined))
    )
  }

}
