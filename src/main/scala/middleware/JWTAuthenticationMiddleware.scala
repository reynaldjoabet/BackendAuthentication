package middleware

import cats.data._
import cats.effect
import cats.effect.IO
import cats.implicits._
import cats.Monad
import cats.MonadThrow

import configs.JWTConfig
import domain._
import org.http4s._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import services.JWTService
import services.JWTServiceLive

object JWTAuthenticationMiddleware {

  private def authenticateUser[F[_]: MonadThrow](
    jwtService: JWTService[F]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], UserID] = Kleisli { req: Request[F] =>
    req.headers.get[Authorization] match {
      case Some(Authorization(Credentials.Token(AuthScheme.Bearer, token))) =>
        // jwt service, verify should return an option
        // OptionT(jwtService.verifyToken1(token))
        OptionT.liftF(jwtService.verifyToken(token))
      case _ => OptionT.none[F, UserID]
    }

  }

  private def authenticateUser2[F[_]: MonadThrow](
    jwtService: JWTService[F]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], UserID] = Kleisli { req: Request[F] =>
    req.headers.get[Authorization] match {
      case Some(Authorization(Credentials.Token(AuthScheme.Bearer, token))) =>
        // jwt service
        OptionT.liftF(jwtService.verifyToken(token)).recoverWith(_ => OptionT.none[F, UserID])
      case _ => OptionT.none[F, UserID]
    }

  }

  private def authenticateUser4[F[_]: MonadThrow](
    jwtService: JWTService[F]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], UserID] = Kleisli { req: Request[F] =>
    req.headers.get[Authorization] match {
      case Some(Authorization(Credentials.Token(AuthScheme.Bearer, token))) =>
        // jwt service
        OptionT.liftF(jwtService.verifyToken(token)).handleErrorWith(_ => OptionT.none[F, UserID])
      case _ => OptionT.none[F, UserID]
    }

  }

  val jwtConfig = JWTConfig(secret = "mysecret", ttl = 864000)
  val clock     = java.time.Clock.systemDefaultZone()

  val jwtMiddleware: AuthMiddleware[IO, UserID] = AuthMiddleware(
    authenticateUser[IO](JWTServiceLive.make(jwtConfig, clock))
  )

  val jwtMiddleware2: AuthMiddleware[IO, UserID] = AuthMiddleware(
    authenticateUser2[IO](JWTServiceLive.make(jwtConfig, clock))
  )

}
