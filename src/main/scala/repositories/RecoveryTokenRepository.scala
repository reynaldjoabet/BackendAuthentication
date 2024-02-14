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
import configs._
import skunk.tuple3ToHList
trait RecoveryTokenRepository[F[_]] {

  def getToken(email: String): F[Option[String]]
  def checkToken(email: String, token: String): F[Boolean]

}

class RecoveryTokenRepositoryLive[F[_]: Concurrent](
    postgres: Resource[F, Session[F]],
    recoveryTokenConfig: RecoveryTokenConfig,
    userRepo: UserRepository[F]
) extends RecoveryTokenRepository[F] {
  override def getToken(email: String): F[Option[String]] =
    userRepo
      .getByEmail(email)
      .flatMap {
        case Some(_) => makeFreshToken(email).map(Some(_))
        case None    => (None: Option[String]).pure
      }

  override def checkToken(email: String, token: String): F[Boolean] = {
    val query: Query[String *: String *: EmptyTuple, PasswordRecoveryToken] =
      sql"""
    SELECT * FROM recovery_tokens WHERE email=$text AND token=$text 
    """
        .query(text ~ text ~ int8) // A~B produces (A,B)
        .map { case (email ~ token ~ expiration) =>
          PasswordRecoveryToken(email, token, expiration)
        }

    postgres.use(
      _.prepare(query).flatMap(_.option(email ~ token).map(_.isDefined))
    )
  }
  private val tokenDuration = recoveryTokenConfig.duration
  private def randomUppercaseString(len: Int): F[String] = {
    Concurrent[F].pure(
      scala.util.Random.alphanumeric.take(len).mkString.toUpperCase
    )
  }

  private def makeFreshToken(email: String): F[String] =
    findToken(email).flatMap {
      case Some(_) => replaceToken(email)
      case None    => generateToken(email)
    }

  import org.typelevel.twiddles.syntax._

  val g = text *: toTwiddleOpTwo(text)

  private val passwordRecoveryTokenEncoder = (text *: text *: int8)
    .contramap[PasswordRecoveryToken] {
      case PasswordRecoveryToken(email, token, expiration) =>
        email *: token *: expiration *: EmptyTuple
    }

  // consider using `a *: b *: c` instead of `a ~ b ~ c`
  private val passwordRecoveryTokenDecoder = (text ~ text ~ int8)
    .map { case (email ~ token ~ expiration) =>
      PasswordRecoveryToken(email, token, expiration)
    }
  private def findToken(email: String): F[Option[String]] = {
    val query = sql"""
    SELECT * FROM recovery_tokens WHERE email= $text
    """.query(passwordRecoveryTokenDecoder)
    postgres.use(_.prepare(query).flatMap(_.option(email)))

  }.map(_.map(_.token))

  // run(
  //   query[PasswordRecoveryToken]
  //     .filter(_.email == lift(email))
  // ).map(_.headOption.map(_.token))// returns a list

  private def findToken1(email: String): F[Option[String]] = {
    val query = sql"""
      SELECT * FROM recovery_tokens WHERE email= $text
      """.query(passwordRecoveryTokenDecoder)
    postgres.use(_.prepare(query).flatMap(_.stream(email, 64).compile.toList))

  }.map(_.headOption.map(_.token))

  private def replaceToken(email: String): F[String] = {
    val query = sql"""
    UPDATE recovery_tokens SET email=$text,token=$text,expiration=$int8
    """.query(passwordRecoveryTokenDecoder)
    for {
      token <- randomUppercaseString(8)

      expiration = java.lang.System.currentTimeMillis() + tokenDuration
      preparedQuery <- postgres.use(_.prepare(query))
      _ <- preparedQuery.unique((email, token, expiration))
    } yield token
  }

  private def generateToken(email: String): F[String] = {
    val query = sql"""
    INSERT INTO recovery_tokens VALUES= $passwordRecoveryTokenEncoder
    """.query(passwordRecoveryTokenDecoder)
    for {
      token <- randomUppercaseString(8)
      expiration = java.lang.System.currentTimeMillis() + tokenDuration
      preparedQuery <- postgres.use(_.prepare(query))
      _ <- preparedQuery.unique(PasswordRecoveryToken(email, token, expiration))
    } yield token

  }
}
