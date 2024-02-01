package service
import domain._
import configs._
import java.time.Instant
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import cats.syntax.all._
import cats.effect.kernel.Sync

trait JWTService[F[_]] {
  def createToken(user: UserJWT): F[UserToken]
  def verifyToken(token: String): F[UserID]
}

final class JWTServiceLive[F[_]: Sync](
    jwtConfig: JWTConfig,
    clock: java.time.Clock
) extends JWTService[F] {

  private val ISSUER = "rockthejvm.com"
  private val CLAIM_USERNAME = "username"
  private val algorithm = Algorithm.HMAC512(jwtConfig.secret)
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
}
