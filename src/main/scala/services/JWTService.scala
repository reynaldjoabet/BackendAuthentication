package services
import domain._
import configs._
import java.time.Instant
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import cats.syntax.all._
import cats.effect.kernel.Sync
import scala.jdk.CollectionConverters._
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
//import scala.jdk.javaapi.CollectionConverters._
trait JWTService[F[_]] {
  def createToken(user: UserJWT): F[UserToken]
  def verifyToken(token: String): F[UserID]
  def verifyToken1(token: String): F[Option[UserID]]
}

final class JWTServiceLive[F[_]: Sync](
    jwtConfig: JWTConfig,
    clock: java.time.Clock
) extends JWTService[F] {

  val salt = "salt".getBytes("UTF-8")

//A user-chosen password that can be used with password-based encryption
  val keySpec = new PBEKeySpec("password".toCharArray(), salt, 65536, 256)
//This class represents a factory for secret keys.
//Secret key factories operate only on secret (symmetric) keys
  val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
  val bytes = factory.generateSecret(keySpec).getEncoded

  private val ISSUER = "rockthejvm.com"
  private val CLAIM_USERNAME = "username"
  private val algorithm = Algorithm.HMAC512(bytes)
  private val verifier = JWT
    .require(algorithm)
    .withIssuer(ISSUER)
    .asInstanceOf[BaseVerification]
    .build(clock)

  override def createToken(user: UserJWT): F[UserToken] = for {
    now <- Sync[F].delay(clock.instant())
    expiration <- Sync[F].pure(now.plusSeconds(jwtConfig.ttl))
    token <- Sync[F].delay(
      JWT
        .create()
        .withIssuer(ISSUER)
        .withIssuedAt(now)
        .withExpiresAt(expiration)
        .withSubject(user.id.toString)
        .withClaim(CLAIM_USERNAME, user.email)
        .withClaim("roles", List("admin").asJava)
        .withClaim("permission", "api:read")
        .sign(algorithm)
    )
  } yield UserToken(user.email, token, expiration.getEpochSecond)

  override def verifyToken(token: String): F[UserID] = for {
    decoded <- Sync[F].delay(verifier.verify(token))
    uid <- Sync[F].delay(
      UserID(
        id = decoded.getSubject.toLong,
        email = decoded.getClaim(CLAIM_USERNAME).asString()
      )
    )
  } yield uid

  override def verifyToken1(token: String): F[Option[UserID]] = (for {
    decoded <- Sync[F].delay(verifier.verify(token))
    uid <- Sync[F].delay(
      UserID(
        id = decoded.getSubject.toLong,
        email = decoded.getClaim(CLAIM_USERNAME).asString()
      )
    )
  } yield uid)
    .map(_.some)
    .recover(_ => None)
}
