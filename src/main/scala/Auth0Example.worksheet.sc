import java.time.LocalDateTime
import java.time.LocalDate
import java.time.Instant
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm

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
