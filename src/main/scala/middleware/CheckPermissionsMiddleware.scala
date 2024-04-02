package middleware

import org.http4s.AuthedRoutes
import cats._
import cats.data._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s._

object CheckPermissionsMiddleware {

sealed abstract class AuthenticationError

case object UnauthorizedResponse extends AuthenticationError

case object ForbiddenResponse extends AuthenticationError
  def onFailure[F[_]: Monad]: AuthedRoutes[AuthenticationError, F] =
    Kleisli { request =>
      val dsl = Http4sDsl[F]
      import dsl._
      request.context match {
        case UnauthorizedResponse =>
          OptionT.liftF(
            Unauthorized.apply(
              `WWW-Authenticate`(Challenge("Bearer", "issuer.toString")),
              request.context.toString()
            )
          )
        case ForbiddenResponse    =>
          OptionT.liftF(Forbidden.apply(""))
      }

    }
}
