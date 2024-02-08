package services
import repositories._
import domain._
import cats.syntax.all._
import cats.Monad
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.SecureRandom
import UserService._
import cats.effect._
trait UserService[F[_]] {
  def registerUser(email: String, password: String): F[UserJWT]
  def verifyPassword(email: String, password: String): F[Boolean]
  def updatePassword(
      email: String,
      oldPassword: String,
      newPassword: String
  ): F[UserJWT]
  def deleteUser(email: String, password: String): F[UserJWT]

  // JWT -- java web token
  def generateToken(email: String, password: String): F[Option[UserToken]]

  // recovery pasword
  def sendPasswdRecoveryToken(email: String): F[Unit]
  def recoverPasswdFromToken(
      email: String,
      token: String,
      newPassword: String
  ): F[Boolean]
}

class UserServiceLive[F[_]: Sync] private (
    userRepo: UserRepository[F],
    tokenRepo: RecoveryTokenRepository[F],
    jwtService: JWTService[F],
    emailService: EmailService[F]
) extends UserService[F] {
  override def registerUser(email: String, password: String): F[UserJWT] =
    userRepo.create(
      UserJWT(1L, email = email, hashedPassword = Hasher.generateHash(password))
    )

  override def verifyPassword(email: String, password: String): F[Boolean] =
    for {
      existingUser <- userRepo.getByEmail(email)
      verified <- existingUser match {
        case None => Sync[F].pure(false)
        case Some(user) =>
          Sync[F]
            .delay(Hasher.validateHash(password, user.hashedPassword))
            .handleError(_ => false)
      }
    } yield verified
// override def updatePassword(
//     email: String,
//     oldPassword: String,
//     newPassword: String
// ): F[UserJWT] = for {
//   optionalUser <- userRepo
//     .getByEmail(email)
//     .orRaise(
//       new RuntimeException(s"Cannot verify the user $email: non existent")
//     )
//   updatedUser <- (optionalUser match {
//     case None => (None: Option[UserJWT]).pure[F]
//     case Some(user) =>
//       Sync[F]
//         .delay(Hasher.validateHash(oldPassword, user.hashedPassword))
//         .ifM(
//           userRepo
//             .update(
//               user.copy(hashedPassword = Hasher.generateHash(newPassword))
//             )
//             .map(_.some),
//           (None: Option[UserJWT]).pure
//         )
//   }).map(_.get)
//     .orRaise((new RuntimeException(s"Could not update password for $email")))
  override def updatePassword(
      email: String,
      oldPassword: String,
      newPassword: String
  ): F[UserJWT] = for {
    existingUser <- userRepo
      .getByEmail(email)
      .map(_.get)
      .orRaise(
        new RuntimeException(s"Cannot verify the user $email: non existent")
      )
    updatedUser <-
      Sync[F]
        .delay(Hasher.validateHash(oldPassword, existingUser.hashedPassword))
        .ifM(
          userRepo
            .update(
              existingUser
                .copy(hashedPassword = Hasher.generateHash(newPassword))
            )
            .map(_.some),
          (None: Option[UserJWT]).pure
        )
        .map(_.get)
        .orRaise(
          (new RuntimeException(s"Could not update password for $email"))
        )

  } yield updatedUser

  override def deleteUser(email: String, password: String): F[UserJWT] = for {
    existingUser <- userRepo
      .getByEmail(email)
      .map(_.get)
      .orRaise(
        new RuntimeException(s"Cannot verify the user $email: non existent")
      )
    updatedUser <-
      Sync[F]
        .delay(Hasher.validateHash(password, existingUser.hashedPassword))
        .ifM(
          userRepo
            .delete(existingUser.id)
            .map(_.some),
          (None: Option[UserJWT]).pure
        )
        .map(_.get)
        .orRaise((new RuntimeException(s"Could not delete user $email")))

  } yield updatedUser

  override def generateToken(
      email: String,
      password: String
  ): F[Option[UserToken]] = for {
    existingUser <- userRepo
      .getByEmail(email)
      .map(_.get)
      .orRaise(
        new RuntimeException(s"Cannot verify the user $email: non existent")
      )
    token <-
      Sync[F]
        .delay(Hasher.validateHash(password, existingUser.hashedPassword))
        .ifM(
          jwtService.createToken(existingUser).map(_.some),
          (None: Option[UserToken]).pure
        )
    // .orRaise((new RuntimeException(s"Could not update password for $email")))

  } yield token

  override def sendPasswdRecoveryToken(email: String): F[Unit] =
    tokenRepo.getToken(email).flatMap {
      case None        => Sync[F].unit
      case Some(token) => emailService.sendPasswordRecoveryEmail(email, token)
    }

  override def recoverPasswdFromToken(
      email: String,
      token: String,
      newPassword: String
  ): F[Boolean] = for {
    existingUser <- userRepo
      .getByEmail(email)
      .map(_.get)
      .orRaise(
        new RuntimeException(s"Cannot verify the user $email: non existent")
      )
    result <- tokenRepo
      .checkToken(email, token)
      .ifM(
        userRepo
          .update(
            existingUser.copy(hashedPassword = Hasher.generateHash(newPassword))
          )
          .map(_.some),
        (None: Option[UserJWT]).pure
      )
      .map(_.isDefined)

  } yield result

}
object UserService {
  // // HMAC   -- Hash Message Authentication Code 基于散列的消息验证模式
  //  // SHA512 -- Secure Hash Algorithm 512, is a hashing algorithm used to convert text of any length into a fixed-size string.
  //  private val PBKDF2_ALGO: String    = "PBKDF2WithHmacSHA512"
  //  private val PBKDF2_ITERATIONS: Int = 1000
  //  private val SALT_BYTE_SIZE: Int    = 24
  //  private val HASH_BYTE_SIZE: Int    = 24
  //  private val skf: SecretKeyFactory  = SecretKeyFactory.getInstance(PBKDF2_ALGO)

  //  private def pbkdf2(
  //      message: Array[Char],
  //      salt: Array[Byte],
  //      iterations: Int,
  //      nBytes: Int
  //  ): Array[Byte] = {

  //    val keySpec: PBEKeySpec = new PBEKeySpec(message, salt, iterations, nBytes * 8)
  //    skf.generateSecret(keySpec).getEncoded
  //  }
  //  /**
  //     * PBKDF2 (Password-Based Key Derivation Function 2) is a key derivation function
  //     * that is part of RSA Laboratories' Public-Key Cryptography Standards (PKCS) series,
  //     * specifically PKCS #5 v2.0, also published as Internet Engineering Task Force's RFC 2898.
  //     */
  //    // string + salt + nIterations PBKDF2
  //    def generateHash(str: String): String = {
  //      val rng  = new SecureRandom()
  //      val salt = Array.ofDim[Byte](SALT_BYTE_SIZE)
  //      rng.nextBytes(salt)
  //      val hashedBytes = pbkdf2(str.toCharArray, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
  //      s"${PBKDF2_ITERATIONS}:${salt.bytes2Hex}:${hashedBytes.bytes2Hex}"
  //    }

  //    def validateHash(str: String, hash: String): Boolean = {
  //      val hashSegments: Array[String] = hash.split(":")
  //      val testHash: Array[Byte] = pbkdf2(
  //        message = str.toCharArray,
  //        salt = hashSegments(1).hex2Bytes,
  //        iterations = hashSegments(0).toInt,
  //        nBytes = HASH_BYTE_SIZE
  //      )

  //      testHash.compareWith(hashSegments(2).hex2Bytes)
  //    }

  object Hasher {

    private val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512"
    private val PBKDF2_ITERATIONS = 1000
    private val SALT_BYTE_SIZE = 24
    private val HASH_BYTE_SIZE = 24
    private val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

    private def pbkdf2(
        message: Array[Char],
        salt: Array[Byte],
        iterations: Int,
        nBytes: Int
    ): Array[Byte] = {
      val keySpec: PBEKeySpec =
        new PBEKeySpec(message, salt, iterations, nBytes * 8)
      skf.generateSecret(keySpec).getEncoded
    }

    private def toHex(array: Array[Byte]): String =
      array.map(b => "%02X".format(b)).mkString

    private def fromHex(string: String): Array[Byte] = {
      string.sliding(2, 2).toArray.map { hexValue =>
        Integer.parseInt(hexValue, 16).toByte
      }
    }
    // a(i) ^ b(i) for every i
    private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean = {
      val range = 0 until math.min(a.length, b.length)
      val diff = range.foldLeft(a.length ^ b.length) { case (acc, i) =>
        acc | (a(i) ^ b(i))
      }
      diff == 0
    }

    // string + salt + nIterations PBKDF2
    def generateHash(string: String): String = {
      val rng: SecureRandom = new SecureRandom()
      val salt: Array[Byte] = Array.ofDim[Byte](SALT_BYTE_SIZE)
      rng.nextBytes(salt) // creates 24 random bytes
      val hashBytes =
        pbkdf2(string.toCharArray, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
      s"$PBKDF2_ITERATIONS:${toHex(salt)}:${toHex(hashBytes)}"
    }

    def validateHash(string: String, hash: String): Boolean = {
      val hashSegments = hash.split(":")
      val nIterations = hashSegments(0).toInt
      val salt = fromHex(hashSegments(1))
      val validHash = fromHex(hashSegments(2))
      val testHash =
        pbkdf2(string.toCharArray, salt, nIterations, HASH_BYTE_SIZE)
      compareBytes(testHash, validHash)
    }
  }
}
