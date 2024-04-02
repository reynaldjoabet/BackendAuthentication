package middleware
import org.http4s.server._
import cats.effect.IO
import cats.data.Kleisli
import org.http4s.ContextRequest
import org.http4s.Request
import org.http4s.ContextRoutes
import org.typelevel.vault.Key

object ff {
import org.http4s
ContextRoutes
  def checkPermissions: ContextMiddleware[IO,String]=contextRoutes=>Kleisli{ request:Request[IO]=>
        //contextRoutes(request)
    

    ???
  }
}
