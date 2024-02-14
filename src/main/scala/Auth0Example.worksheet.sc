import cats.effect.unsafe.IORuntime
import cats.data.OptionT
import cats.effect.IO
import java.util.Base64
import java.security.MessageDigest
import cats.effect.kernel.Async
import org.http4s.SameSite
import org.http4s.ResponseCookie
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.Instant
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import domain.User
import io.circe.syntax.EncoderOps
val algo = Algorithm.HMAC512("secret")
val jwt = JWT
  .create()
  .withIssuer("rockthejvm.com")
  .withIssuedAt(Instant.now())
  .withExpiresAt(Instant.now().plusSeconds(24 * 60 * 60))
  .withSubject("232323") // user identifier
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

Instant.now()
LocalDate.now()

//LocalDateTime.now().toInstant()

import scala.concurrent.duration._
val COOKIE_NAME = ""

def createCookie(sessionId: String): ResponseCookie =
  ResponseCookie(
    name = COOKIE_NAME /* jwtCookie  */,
    content = sessionId /* jwt cookie */,
    expires = None,
    maxAge = Some(1.hour.toSeconds),
    path = Some("/"),
    sameSite = Some(SameSite.Lax),
    secure = true,
    httpOnly = true,
    // domain = None
    domain = Some("localhost")
  )
//The server creates a session, session token, and cookie
//The server then stores the session and the session
//token into a database.
//The server then sends the cookie that internally
//contains the session token back to the browser
private def generateSessionToken(token: String): IO[String] =
  Async[IO].delay {
    val mac = MessageDigest.getInstance("SHA3-512")
    val digest = mac.digest(token.getBytes())
    // a 160-bit (20 byte) random value that is then URL-safe base64-encoded
    // byte[] buffer = new byte[20];
    Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    // Base64.getEncoder().encodeToString(digest)
  }

// Base64.getUrlEncoder().withoutPadding()
//                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))

// val h=Base64.getUrlEncoder().withoutPadding()
//                .encodeToString("payloadJson".getBytes())

// generateSessionToken(User(1L,"john",Set.empty[String]).toString())

OptionT.liftF(IO(Some(3))).value

implicit val ec: IORuntime = IORuntime.global
generateSessionToken(User(1L, "john", Set.empty[String]).toString())
  .unsafeRunSync()

val user = User(1L, "Peter", Set.empty)

user.asJson.spaces2

user.asJson.noSpaces
user.asJson.spaces2SortKeys

user.asJson.spaces4

user.asJson.noSpacesSortKeys

import io.circe.Decoder

"{\"id\":1,\"roles\":[],\"username\":\"Peter\"}"
