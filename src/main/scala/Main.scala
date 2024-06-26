import java.time.Instant

import cats.effect._

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import configs._
import domain._
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import services.JWTServiceLive

object Main extends IOApp {

  val algo = Algorithm.HMAC512("secret")

  val salt = "salt".getBytes("UTF-8")
  // A user-chosen password that can be used with password-based encryption
  val keySpec = new PBEKeySpec("password".toCharArray(), salt, 65536, 256)
  // This class represents a factory for secret keys.
  // Secret key factories operate only on secret (symmetric) keys
  val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
  val bytes   = factory.generateSecret(keySpec).getEncoded

  val jwt: String = JWT
    .create()
    .withIssuer("rockthejvm.com")
    .withIssuedAt(Instant.now())
    .withExpiresAt(Instant.now().plusSeconds(24 * 60 * 60))
    .withSubject("1") // user identifier
    .withClaim("username", "daniel@rockthejvm.com").sign(algo)

  val verifier = JWT
    .require(algo)
    .withIssuer("rockthejvm.com")
    .asInstanceOf[BaseVerification]
    .build(java.time.Clock.systemDefaultZone())

  val decoded   = verifier.verify(jwt)
  val userId    = decoded.getSubject
  val userEmail = decoded.getClaim("username").asString()

  val jwtConfig = JWTConfig(secret = "mysecret", ttl = 864000)
  val clock     = java.time.Clock.systemDefaultZone()
//import cats.effect.Clock

  val program = for {
    service   <- IO(JWTServiceLive.make[IO](jwtConfig, clock))
    userToken <- service.createToken(UserJWT(1L, "daniel@rockthejvm.com", ""))
    _         <- IO.println(userToken)
    uid       <- service.verifyToken(userToken.token).recover(_ => UserID(2L, "", Set.empty[Role]))
    _         <- IO.println(uid.toString)

  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

}
