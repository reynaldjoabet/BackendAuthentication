package middleware

import cats.data._
import cats.effect.syntax.all._
import cats.implicits._
import cats.syntax.all.catsSyntaxApplicativeError
import cats.Monad
import cats.MonadError
import cats.MonadThrow

import domain._
import io.circe.Decoder
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import services._

object CookieAuthenticationMiddleware {

  // Role based authorization checks:
//Are declarative and specify roles which the current user must be a member of to access the requested resource.
  private def authenticateUser[F[_]: MonadThrow](
    userSessionService: UserSessionService[F],
    roles: Set[String]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], User] = Kleisli { req: Request[F] =>
    req
      .cookies
      .filter(_.name == "XSESSION")
      .headOption
      .map(_.content)
      .collect { case sessionId /* cookie */ =>
        OptionT
          .liftF(userSessionService.getUserSession(sessionId))
          .flatMap {
            case Some(user) if roles.forall(user.roles.contains) =>
              OptionT.pure[F](user)
            case _ => OptionT.none[F, User]
          }
      }
      .getOrElse(OptionT.none[F, User])
  }
// val authUser: Kleisli[F, Request[F], Either[String, A]] =
//      Kleisli { request =>
//        AuthHeaders.getBearerToken(request).fold("Bearer token not found".asLeft[A].pure[F]) { token =>
//          jwtDecode[F](token, jwtAuth)
//            .flatMap(authenticate(token))
//            .map(_.fold("not found".asLeft[A])(_.asRight[String]))
//            .recover {
//              case _: JwtException => "Invalid access token".asLeft[A]
//            }
//        }
//      }

  sealed abstract class AuthenticationError

  case object UnauthorizedResponse extends AuthenticationError

  case object ForbiddenResponse extends AuthenticationError

  private def authenticateUser5[F[_]: MonadThrow](
    userSessionService: UserSessionService[F],
    roles: Set[String]
  ): Kleisli[F, Request[F], Either[String, User]] = Kleisli { req: Request[F] =>
    req
      .cookies
      .filter(_.name == "XSESSION")
      .headOption
      .map(_.content)
      .fold("Unauthorized".asLeft[User].pure[F]) { case sessionId /* cookie */ =>
        userSessionService
          .getUserSession(sessionId)
          .map {
            case Some(user) if roles.subsetOf(user.roles) =>
              Right[String, User](user)
            case Some(user) => "Forbidden".asLeft[User]
            case None       => "Unauthorized".asLeft[User]
          }
          .recover { case _ =>
            "Invalid access token".asLeft[User]
          }
      }

  }

  /**
    * An origin server that wishes to hide the current existence of a forbidden target resource MAY
    * instead respond with a status code of 404
    */

  private def authenticateUser6[F[_]: MonadThrow](
    userSessionService: UserSessionService[F],
    requiredRoles: Set[String]
  ): Kleisli[F, Request[F], Either[AuthenticationError, User]] = Kleisli { req: Request[F] =>
    req
      .cookies
      .filter(_.name == "XSESSION")
      .headOption
      .map(_.content)
      .fold(
        (UnauthorizedResponse: AuthenticationError).asLeft[User].pure[F]
      ) { case sessionId /* cookie */ =>
        userSessionService
          .getUserSession(sessionId)
          .map {
            case Some(user) if requiredRoles.subsetOf(user.roles) =>
              Either.right[AuthenticationError, User](user)
            case Some(user) =>
              Either.left[AuthenticationError, User](ForbiddenResponse)
            case None =>
              Either.left[AuthenticationError, User](UnauthorizedResponse)
          }
          .recover { case _ =>
            Either.left[AuthenticationError, User](UnauthorizedResponse)
          }
      }

  }

  def apply2[F[_]: MonadThrow](
    userSessionService: UserSessionService[F]
  ): AuthMiddleware[F, User] =
    AuthMiddleware(
      authenticateUser6[F](userSessionService, Set("ADMIN")),
      onFailure[F]
    )

  private def authenticateUser3[F[_]: Monad](
    cookieRepo: CookieRepository[F, User],
    roles: Set[String]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], User] = Kleisli { req: Request[F] =>
    req.cookies.filter(_.name == "cookiename").headOption.map(_.content) match {
      case Some(cookie) =>
        OptionT
          .liftF(cookieRepo.findUserByCookie(cookie))
          .flatMap {
            case Some(user) if roles.subsetOf(user.roles) =>
              OptionT.pure[F](user)
            case _ => OptionT.none[F, User]
          }
      case None => OptionT.none[F, User]
    }
  }

  private def authenticateUser35[F[_]: Monad, T: Decoder](
    redisService: RedisService[F]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], T] = Kleisli { req: Request[F] =>
    req.cookies.find(_.name == "cookiename").map(_.content) match {
      case Some(cookie) =>
        OptionT
          .liftF(redisService.get[T](cookie))
          .flatMap {
            case Some(value) =>
              OptionT.pure[F](value)
            case _ => OptionT.none[F, T]
          }
      case None => OptionT.none[F, T]
    }
  }

  private def authenticateUser351[F[_]: Monad, T: Decoder](
    redisService: RedisService[F]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], T] = Kleisli { req: Request[F] =>
    req.cookies.find(_.name == "cookiename").map(_.content) match {
      case Some(cookie) =>
        OptionT(redisService.get[T](cookie)).flatMap(OptionT.pure[F](_))
      case None => OptionT.none[F, T]
    }
  }

  private def authenticateUser40[F[_]: Monad](
    jwtService: JWTService[F]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], UserID] = Kleisli { req: Request[F] =>
    req.cookies.find(_.name == "cookiename").map(_.content) match {
      case Some(cookie) =>
        OptionT
          .liftF(jwtService.verifyToken1(cookie))
          .flatMap {
            case Some(userId) =>
              OptionT.pure[F](userId)
            case _ => OptionT.none[F, UserID]
          }
      case None => OptionT.none[F, UserID]
    }
  }

  import domain._

  def authMiddleware35[F[_]: Monad](redisService: RedisService[F]): AuthMiddleware[F, User] =
    AuthMiddleware(authenticateUser35[F, User](redisService))

  def authMiddleware36[F[_]: Monad, T: Decoder](
    redisService: RedisService[F]
  ): AuthMiddleware[F, T] =
    AuthMiddleware(authenticateUser35[F, T](redisService))

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
        case ForbiddenResponse =>
          OptionT.liftF(Forbidden.apply(""))
      }

    }

  // scoped based authorization
  // be aware that scopes are authorizing clients and not users in OAuth.
  // A scope allows the client to invoke the functionality associated with the scope and is unrelated to the user's permission to do so
  // This additional user centric authorization is application logic and not covered by OAuth, yet still possibly important to implement in your API.
  private def authenticateUser4[F[_]: Monad](
    cookieRepo: CookieRepository[F, UserWithScopes],
    requiredScopes: Set[String]
  ): Kleisli[({ type Y[X] = OptionT[F, X] })#Y, Request[F], UserWithScopes] =
    Kleisli { req: Request[F] =>
      req.cookies.filter(_.name == "cookiename").headOption.map(_.content) match {
        case Some(cookie) =>
          OptionT
            .liftF(cookieRepo.findUserByCookie(cookie))
            .flatMap {
              case Some(user) if requiredScopes.forall(user.scopes.contains) => // requiredScopes.subsetOf(user.scopes) =>
                OptionT.pure[F](user)
              case _ => OptionT.none[F, UserWithScopes]
            }
        case None => OptionT.none[F, UserWithScopes]
      }
    }

//   // Role and claim-based authorization is a crucial aspect of securing applications by allowing access control based on roles and specific claims assigned to users.
//   private def authenticateUser4[F[_]: Monad](
//       cookieRepo: CookieRepository[F, UserWithPermissions],
//       requiredPermissions: Set[Permission]
//   ): Kleisli[OptionT[F, *], Request[F], UserWithPermissions] = Kleisli { req: Request[F] =>
//     req.cookies.filter(_.name == "cookiename").headOption.map(_.content) match {
//       case Some(cookie) =>
//         OptionT.liftF(cookieRepo.findUserByCookie(cookie)).flatMap {
//           case Some(user) if requiredPermissions.subsetOf(user.permissions) => OptionT.pure[F](user)
//           case _                                                            => OptionT.none[F, UserWithPermissions]
//         }
//       case None => OptionT.none[F, UserWithPermissions]
//     }
//   }

// Example usage:
  def apply[F[_]: MonadThrow](
    userSessionService: UserSessionService[F]
  ): AuthMiddleware[F, User] =
    AuthMiddleware(
      authenticateUser6[F](userSessionService, Set("ADMIN")),
      onFailure[F]
    )

  // Example usage:
  def authMiddleware3[F[_]: Monad](
    tokenRepo: CookieRepository[F, User]
  ): AuthMiddleware[F, User] =
    AuthMiddleware(
      authenticateUser3[F](
        tokenRepo,
        Set("admin:read", "account:update", "account:delete", "account:create")
      )
    )

  trait CookieRepository[F[_], T] {
    def findUserByCookie(token: String): F[Option[T]]
  }

  final abstract class RedisImplementation[F[_]: Monad, T] extends CookieRepository[F, T] {
    override def findUserByCookie(token: String): F[Option[T]]

  }

}
