package middleware

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.syntax.all._
import cats.syntax.all._

import dev.profunktor.redis4cats.RedisCommands
import domain._

sealed trait UserSessionService[F[_]] {

  def getUserSession(sessionId: String): F[Option[User]]
  def deleteUserSession(sessionId: String): F[Long]

  def setUserSession(
    ket: String,
    value: User,
    expiration: Option[FiniteDuration] = None
  ): F[Unit]

}

object UserSessionService {

  def make[F[_]: Async](
    redisCommands: RedisCommands[F, String, User]
  ) = new UserSessionService[F] {

    override def getUserSession(sessionId: String): F[Option[User]] =
      redisCommands.get(sessionId)

    override def deleteUserSession(sessionId: String): F[Long] =
      redisCommands.del(sessionId)

    private def renewSession(sessionId: String): F[Option[User]] =
      getUserSession(sessionId).flatMap {
        case Some(session) => ???

        case None => Async[F].pure(None)
      }

    override def setUserSession(
      ket: String,
      value: User,
      expiration: Option[FiniteDuration] = None
    ): F[Unit] =
      expiration match {
        case None      => redisCommands.set(ket, value)
        case Some(exp) => redisCommands.setEx(ket, value, exp)
      }

  }

}
