import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Base64
import java.util.Locale

import cats.data.OptionT
import cats.effect.kernel.Async
import cats.effect.unsafe.IORuntime
import cats.effect.IO

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import domain.User
import io.circe.syntax.EncoderOps
import org.http4s.ResponseCookie
import org.http4s.SameSite

val algo = Algorithm.HMAC512("secret")

val jwt = JWT
  .create()
  .withIssuer("rockthejvm.com")
  .withIssuedAt(Instant.now())
  .withExpiresAt(Instant.now().plusSeconds(24 * 60 * 60))
  .withSubject("232323") // user identifier
  .withClaim("username", "daniel@rockthejvm.com").sign(algo)

val verifier = JWT
  .require(algo)
  .withIssuer("rockthejvm.com")
  .asInstanceOf[BaseVerification]
  .build(java.time.Clock.systemDefaultZone())

val decoded   = verifier.verify(jwt)
val userId    = decoded.getSubject
val userEmail = decoded.getClaim("username").asString()

Instant.now()
LocalDate.now()

//LocalDateTime.now().toInstant()

import scala.concurrent.duration._
val COOKIE_NAME = "XSESSION"

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
    val mac    = MessageDigest.getInstance("SHA3-512")
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
generateSessionToken(User(1L, "john900", Set.empty[String]).toString()).unsafeRunSync()
generateSessionToken(User(1L, "john", Set.empty[String]).toString()).unsafeRunSync()
generateSessionToken(User(1L, "john0", Set.empty[String]).toString()).unsafeRunSync()
import scala.util.Random

Random.alphanumeric.take(86).mkString.toLowerCase()

scala.util.Random.alphanumeric.take(86).mkString.toLowerCase()

scala.util.Random.alphanumeric.take(86).mkString.toLowerCase()
Base64.getUrlEncoder().withoutPadding()

Random.alphanumeric.take(86).mkString.toLowerCase(Locale.US)

Random.alphanumeric.take(86).mkString.toLowerCase(Locale.CANADA_FRENCH) //.encodeToString()
import scala.util.chaining._

// Define a function that computes the SHA3-512 hash digest and then encodes it to Base64
val sha3_512HashAndEncode: String => String = (input: String) => {
  // Compute the SHA3-512 hash digest of the input
  val sha3_512Digest = MessageDigest.getInstance("SHA3-512").digest(input.getBytes("UTF-8"))
  // Encode the digest to a URL-safe Base64 string without padding
  Base64.getUrlEncoder.withoutPadding.encodeToString(sha3_512Digest)
}

val sha3_512HashAndEncode3: String => String = (input: String) =>
  MessageDigest
    .getInstance("SHA3-512")
    .digest(input.getBytes("UTF-8"))
    .pipe(Base64.getUrlEncoder.withoutPadding.encodeToString)
// Encode the digest to a URL-safe Base64 string without padding

val sha3_512HashAndEncode7: String => String = (input: String) =>
  MessageDigest
    .getInstance("SHA3-512")
    .digest(input.getBytes("UTF-8"))
    .pipe(
      Base64.getUrlEncoder.withoutPadding.encodeToString
    )

val user = User(1L, "Peter", Set.empty)

sha3_512HashAndEncode7(user.asJson.noSpaces)

sha3_512HashAndEncode7(user.asJson.noSpaces)

user.asJson.spaces2

user.asJson.noSpaces
user.asJson.spaces2SortKeys

user.asJson.spaces4

user.asJson.noSpacesSortKeys

import io.circe.Decoder

"{\"id\":1,\"roles\":[],\"username\":\"Peter\"}"

val set1 = (5 to 10).toSet

val set2 = (1 to 5).toSet
//Tests whether a predicate holds for all elements of this $coll.
set1.forall(set2.contains)

set2.forall(set1.contains)

set1.exists(set2.contains)

createCookie(Random.alphanumeric.take(100).mkString)

createCookie(Random.alphanumeric.take(100).mkString)

createCookie(Random.alphanumeric.take(100).mkString)

createCookie(Random.alphanumeric.take(100).mkString)

import cats.syntax.all._

OptionT[IO, Int](IO(Some(3))).value.unsafeRunSync()

OptionT[IO, Int](IO(None)).value.unsafeRunSync()

//OptionT[IO,Int](IO(Some( 8/0))).value.unsafeRunSync()

OptionT[IO, Int](IO(Some(1)))
  .flatMap(i => OptionT[IO, Int](IO(Some(i * 485))))
  .value
  .unsafeRunSync()

OptionT[IO, Int](IO(Some(3)))
  .flatMap(i => OptionT[IO, Int](IO(Some(i * 485))))
  .value
  .unsafeRunSync()
OptionT[IO, Int](IO(None)).flatMap(i => OptionT[IO, Int](IO(Some(i * 485)))).value.unsafeRunSync()

30.minutes.toSeconds

"2022-02-15T18:35:24.00Z".takeWhile(_ != 'T')

val bar = valueOf[23]
// bar is 23.type = 23

//Class[Int]

"password".getBytes
