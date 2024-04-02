package middleware

import org.http4s.server.AuthMiddleware
import cats.effect.IO
import services.JWTService
import cats.data._
import cats.MonadThrow
import org.http4s._
import org.http4s.headers.Authorization
import cats.syntax.all._
import domain._

object RBACMiddleware {
 
    def authenticateUser[F[_]: MonadThrow](
      jwtService: JWTService[F],rolesAllowed: Set[Role]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], UserID] = Kleisli {
    req: Request[F] =>
      req.headers.get[Authorization] match {
        case Some(Authorization(Credentials.Token(AuthScheme.Bearer, token))) =>
    
          OptionT.liftF(jwtService.verifyToken(token)).flatMap{ user=>
            if(rolesAllowed.forall(user.roles.contains))OptionT.pure(user) else OptionT.none[F, UserID]

          }
            .recoverWith(_ => OptionT.none[F, UserID])
        case _                                                                => OptionT.none[F, UserID]
      }

  }

   //Define RBAC middleware
def apply(jwtService: JWTService[IO],rolesAllowed: Set[Role]): AuthMiddleware[IO,UserID] = AuthMiddleware(authenticateUser(jwtService,rolesAllowed))


def adminRBACMiddleware(jwtService: JWTService[IO])= apply(jwtService,Set(Role.Admin,Role.User,Role.Moderator))
}
