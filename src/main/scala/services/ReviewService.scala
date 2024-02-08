package services
import requests._
import repositories._
import domain._
import java.time.Instant
trait ReviewService[F[_]] {

  def create(req: CreateReviewRequest, userId: Long): F[Review]
  def getById(id: Long): F[Option[Review]]
  def getByCompanyId(companyId: Long): F[List[Review]]
  def getByUserId(userId: Long): F[List[Review]]
}

class ReviewServiceLive[F[_]] private (repo: ReviewRepository[F])
    extends ReviewService[F] {
  override def create(req: CreateReviewRequest, userId: Long): F[Review] =
    repo.create(
      new Review(
        1L,
        req.companyId,
        userId,
        req.management,
        req.culture,
        req.salary,
        req.benefits,
        req.wouldRecommend,
        req.review,
        Instant.now,
        Instant.now
      )
    )

  override def getById(id: Long): F[Option[Review]] =
    repo.getById(id)

  override def getByCompanyId(companyId: Long): F[List[Review]] =
    repo.getByCompanyId(companyId)

  override def getByUserId(userId: Long): F[List[Review]] =
    repo.getByUserId(userId)

}
