package repositories

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import cats.effect._
import cats.syntax.all._

import domain._
import skunk._
//import skunk.implicits._
import skunk.codec.all._
import skunk.data.Type
import skunk.syntax.all._

trait UserRepository[F[_]] {

  def create(user: UserJWT): F[UserJWT]
  def getById(id: Long): F[Option[UserJWT]]
  def getByEmail(email: String): F[Option[UserJWT]]
  def update(user: UserJWT): F[UserJWT]
  def delete(id: Long): F[UserJWT]

}

class UserRepositoryLive[F[_]: Concurrent](
  postgres: Resource[F, Session[F]]
) extends UserRepository[F] {

  private val instantCodec: Codec[Instant] = timestamp.imap(
    _.toInstant(ZoneOffset.UTC)
  )(LocalDateTime.ofInstant(_, ZoneOffset.UTC))

  val userEncoder = (int8 ~ text ~ text ~ instantCodec ~ instantCodec)
    .values
    .contramap[UserJWT] { case UserJWT(id, email, hashedPassword, ctime, mtime) =>
      id ~ email ~ hashedPassword ~ ctime ~ mtime
    }

  val userDecoder = (int8 ~ text ~ text ~ instantCodec ~ instantCodec).map {
    case (id ~ email ~ hashedPassword ~ ctime ~ mtime) =>
      UserJWT(id, email, hashedPassword, ctime, mtime)
  }

  override def create(user: UserJWT): F[UserJWT] = {

    val query: Query[UserJWT, UserJWT] = sql"""
    INSERT INTO users(id,email,hashedPassword,ctime,mtime) VALUES =$userEncoder
    """.query(userDecoder)

    postgres.use(_.prepare(query).flatMap(_.unique(user)))

  }

  override def getById(id: Long): F[Option[UserJWT]] = {
    val query = sql"""
    SELECT * from users where id=$int8
    """.query(userDecoder)

    postgres.use(_.prepare(query).flatMap(_.option(id)))
  }

  override def getByEmail(email: String): F[Option[UserJWT]] = {
    val query = sql"""
       SELECT * from users where email=$text
       """.query(userDecoder)

    postgres.use(_.prepare(query).flatMap(_.option(email)))
  }

  override def update(user: UserJWT): F[UserJWT] = {
    val query = sql"""
    UPDATE users set email=$text,hashedPassword=$text,ctime=$instantCodec,mtime=$instantCodec WHERE id=$int8
    """.query(userDecoder)

    postgres.use(
      _.prepare(query)
        .flatMap(
          _.unique(
            user.email *: user.hashedPassword *: user.ctime *: user.mtime *: user.id *: EmptyTuple
          )
        )
    )
  }

  override def delete(id: Long): F[UserJWT] = {
    val query = sql"""
    DELETE FROM users WHERE id=$int8
    """.query(userDecoder)

    postgres.use(_.prepare(query).flatMap(_.unique(id)))
  }

}
