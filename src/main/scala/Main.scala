import cats.effect._
import java.time.Instant
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import domain._
import configs._
import service._

object Main extends IOApp {
  val algo = Algorithm.HMAC512("secret")
  val jwt = JWT
    .create()
    .withIssuer("rockthejvm.com")
    .withIssuedAt(Instant.now())
    .withExpiresAt(Instant.now().plusSeconds(24 * 60 * 60))
    .withSubject("1") // user identifier
    .withClaim("username", "daniel@rockthejvm.com")
    .sign(algo)

  val verifier = JWT
    .require(algo)
    .withIssuer("rockthejvm.com")
    .asInstanceOf[BaseVerification]
    .build(java.time.Clock.systemDefaultZone())

  val decoded = verifier.verify(jwt)
  val userId = decoded.getSubject
  val userEmail = decoded.getClaim("username").asString()

  val jwtConfig = JWTConfig(secret = "mysecret", ttl = 864000)
  val clock = java.time.Clock.systemDefaultZone()
//import cats.effect.Clock

  val program = for {
    service <- IO(new JWTServiceLive[IO](jwtConfig, clock))
    userToken <- service.createToken(UserJWT(1L, "daniel@rockthejvm.com", ""))
    _ <- IO.println(userToken)
    uid <- service.verifyToken(userToken.token)
    _ <- IO.println(uid.toString)

  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

}