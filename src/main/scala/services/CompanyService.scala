package services
import requests._
import repositories._
import domain._
trait CompanyService[F[_]] {
  def create(req: CreateCompanyRequest): F[Company]
  def getAll: F[List[Company]]
  def getById(id: Long): F[Option[Company]]
  def getBySlug(slug: String): F[Option[Company]]
}

class CompanyServiceLive[F[_]] private (repo: CompanyRepository[F])
    extends CompanyService[F] {
  override def create(req: CreateCompanyRequest): F[Company] =
    repo.create(
      new Company(
        1L,
        req.name,
        req.name,
        req.url,
        req.location,
        req.country,
        req.industry,
        req.image,
        req.tags.getOrElse(List.empty[String])
      )
    )

  override def getAll: F[List[Company]] =
    repo.getAll

  override def getById(id: Long): F[Option[Company]] =
    repo.getById(id)

  override def getBySlug(slug: String): F[Option[Company]] =
    repo.getBySlug(slug)

}
