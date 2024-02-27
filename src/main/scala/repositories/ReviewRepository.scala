package repositories

import java.time.Instant
import domain._

import cats.effect._
import skunk._
import skunk.syntax.all._
//import skunk.implicits._
import cats.syntax.all._
import skunk.codec.all._
import java.time.ZoneOffset
import java.time.LocalDateTime

trait ReviewRepository[F[_]] {
  def create(review: Review): F[Review]
  def getById(id: Long): F[Option[Review]]
  def getByCompanyId(companyId: Long): F[List[Review]]
  def getByUserId(userId: Long): F[List[Review]]
  def update(id: Long, review: Review): F[Review]
  def delete(id: Long): F[Review]
}
// id: Long, // PK
//     companyId: Long,
//     userId: Long, // FK
//     management: Int, // 1-5
//     culture: Int,
//     salary: Int,
//     benefits: Int,
//     wouldRecommend: Int,
//     review: String,
//     created: Instant,
//     updated: Instant
class ReviewRepositoryLive[F[_]: Concurrent] private (
    postgres: Resource[F, Session[F]]
) extends ReviewRepository[F] {

  private val instantCodec: Codec[Instant] = timestamp.imap(
    _.toInstant(ZoneOffset.UTC)
  )(LocalDateTime.ofInstant(_, ZoneOffset.UTC))

  val reviewEncoder         =
    (int8 ~ int8 ~ int8 ~ int4 ~ int4 ~ int4 ~ int4 ~ int4 ~ text ~ instantCodec ~ instantCodec).values
      .contramap[Review] {
        case Review(
              id,
              companyId,
              userId,
              management,
              culture,
              salary,
              benefits,
              wouldRecommend,
              review,
              created,
              updated
            ) =>
          id ~ companyId ~ userId ~ management ~ culture ~ salary ~ benefits ~ wouldRecommend ~ review ~ created ~ updated
      }
  private val reviewDecoder =
    (int8 ~ int8 ~ int8 ~ int4 ~ int4 ~ int4 ~ int4 ~ int4 ~ text ~ instantCodec ~ instantCodec)
      .map {
        case id ~ companyId ~ userId ~ management ~ culture ~ salary ~ benefits ~ wouldRecommend ~ review ~ created ~ updated =>
          Review(
            id,
            companyId,
            userId,
            management,
            culture,
            salary,
            benefits,
            wouldRecommend,
            review,
            created,
            updated
          )
      }

  override def create(review: Review): F[Review] = {

    // val query = sql"""
    // INSERT INTO reviews id=$int8,
    //         companyId=$int8,
    //         userId=$int8,
    //         management=$int4,
    //         culture=$int4,
    //         salary=$int4,
    //         benefits=$int4,
    //         wouldRecommend=$int8,
    //         review=$text,
    //         created=$instantCodec,
    //         updated=$instantCodec
    // """.query(reviewDecoder)
    val query = sql"""
INSERT INTO reviews (id,companyId,userId,management,culture,salary,benefits,wouldRecommend,review,created,updated)
 VALUES= $reviewEncoder
""".query(reviewDecoder)

    postgres.use(_.prepare(query).flatMap(_.unique(review)))

  }

  override def getById(id: Long): F[Option[Review]] = {
    val query = sql"""
    SELECT * FROM reviews WHERE id=$int8
    """.query(reviewDecoder)

    postgres.use(_.prepare(query).flatMap(_.option(id)))
  }

  override def getByCompanyId(companyId: Long): F[List[Review]] = {
    val query = sql"""
    SELECT * FROM reviews WHERE companyId=$int8
    """.query(reviewDecoder)

    postgres.use(
      _.prepare(query).flatMap(_.stream(companyId, 64).compile.toList)
    )
  }

  override def getByUserId(userId: Long): F[List[Review]] = {
    val query = sql"""
    SELECT * FROM reviews WHERE userId=$int8
    """.query(reviewDecoder)

    postgres.use(_.prepare(query).flatMap(_.stream(userId, 64).compile.toList))
  }

  override def update(id: Long, review: Review): F[Review] = {
    val query = sql"""
    UPDATE reviews SET
            companyId=$int8,
            userId=$int8,
            management=$int4,
            culture=$int4,
            salary=$int4,
            benefits=$int4,
            wouldRecommend=$int4,
            review=$text,
            created=$instantCodec,
            updated=$instantCodec WHERE id=$int8
    """.query(reviewDecoder)

    postgres.use(
      _.prepare(query).flatMap(
        _.unique(
          review.companyId *: review.userId *: review.management *: review.culture *: review.salary *: review.benefits *: review.wouldRecommend *: review.review *: review.created *: review.updated *: review.id *: EmptyTuple
        )
      )
    )
  }

  override def delete(id: Long): F[Review] = {
    val query = sql"""
    DELETE FROM reviews WHERE id=$int8
    """.query[Review](reviewDecoder)

    postgres.use(_.prepare(query).flatMap(_.unique(id)))
  }

}

object ReviewRepositoryLive {
  def make[F[_]: Concurrent](
      postgres: Resource[F, Session[F]]
  ) = new ReviewRepositoryLive[F](postgres)
}
