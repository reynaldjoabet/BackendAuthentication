package domain
import java.time.Instant

final case class UserJWT(
    id: Long, // PK
    email: String,
    hashedPassword: String,
    ctime: Instant = Instant.now(),
    mtime: Instant = Instant.now()
)
