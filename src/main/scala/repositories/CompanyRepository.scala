package repositories

import cats.effect._
import cats.syntax.all._

import domain._
import doobie.util.pos
import skunk._
//import skunk.implicits._
import skunk.codec.all._
//import skunk.data.Type
import skunk.data.Arr
import skunk.syntax.all._

//import natchez.Trace.Implicits.noop
trait CompanyRepository[F[_]] {

  def create(company: Company): F[Company]
  def create1(company: Company): F[Unit]
  def update(id: Long, company: Company): F[Company]
  def delete(id: Long): F[Company]
  def getById(id: Long): F[Option[Company]]
  def getBySlug(slug: String): F[Option[Company]]
  def getAll: F[List[Company]]

}
// slug: String,
//     name: String,
//     url: String,
//     location: Option[String] = None,
//     country: Option[String] = None,
//     industry: Option[String] = None,
//     image: Option[String] = None,
//     tags: List[String] = List.empty

import cats.effect.Concurrent

class CompanyRepositoryLive[F[_]: Concurrent] private (
  postgres: Resource[F, Session[F]]
) extends CompanyRepository[F] {

  private val companyEncoder: Encoder[Company] =
    (int8 ~ text ~ text ~ text ~ text.opt ~ text.opt ~ text.opt ~ text.opt ~ _text)
      .values
      .contramap[Company] {
        case Company(
              id,
              slug,
              name,
              url,
              location,
              country,
              industry,
              image,
              tags
            ) =>
          id ~ slug ~ name ~ url ~ location ~ country ~ industry ~ image ~ Arr(
            tags: _*
          )
      }

  private val companyEncoder2: Encoder[Company] =
    (text ~ text ~ text ~ text.opt ~ text.opt ~ text.opt ~ text.opt ~ _text ~ int8)
      .values
      .contramap[Company] {
        case Company(
              id,
              slug,
              name,
              url,
              location,
              country,
              industry,
              image,
              tags
            ) =>
          slug ~ name ~ url ~ location ~ country ~ industry ~ image ~ Arr(
            tags: _*
          ) ~ id
      }

  private val companyUpdateEncoder: Encoder[CompanyUpdate] =
    (text ~ text ~ text ~ text.opt ~ text.opt ~ text.opt ~ text.opt ~ _text)
      .values
      .contramap[CompanyUpdate] {
        case CompanyUpdate(
              slug,
              name,
              url,
              location,
              country,
              industry,
              image,
              tags
            ) =>
          slug ~ name ~ url ~ location ~ country ~ industry ~ image ~ Arr(
            tags: _*
          )
      }

  val f = companyUpdateEncoder ~ int8

  private val companyDecoder: Decoder[Company] =
    (int8 ~ text ~ text ~ text ~ text.opt ~ text.opt ~ text.opt ~ text.opt ~ _text).map {
      case id ~ slug ~ name ~ url ~ location ~ country ~ industry ~ image ~ tags =>
        Company(
          id,
          slug,
          name,
          url,
          location,
          country,
          industry,
          image,
          tags.flattenTo(List)
        )
    }

  override def create(company: Company): F[Company] = {
    val query: Query[Company, Company] = sql"""
        INSERT INTO companies(id,slug,name,url,location,country,industry,image,tags) VALUES $companyEncoder
        """".query[Company](companyDecoder)

    // val query2=sql"""
    //          INSERT INTO company(id,slug,name,url,location,country,industry,image,tags) VALUES $companyEncoder
    //          """".query(int8~ text~ text~text~text.opt~text.opt~text.opt~text.opt~ _text)
    //         .to[Company]// not working

    // postgres.use(
    //   _.prepare(query).flatMap(
    //     _.stream(company, 1024).compile.toList.map(_.head)
    //   )
    // )
    postgres.use(
      _.prepare(query)
        .flatMap(
          _.unique(company)
        )
    )
  }

  override def create1(company: Company): F[Unit] = {
    val command: Command[Company] = sql"""
              INSERT INTO companies(id,slug,name,url,location,country,industry,image,tags) VALUES $companyEncoder
              """".command

    postgres.use(_.prepare(command).flatMap(_.execute(company)).void)
  }

  override def update(id: Long, company: Company): F[Company] = {
    val query: Query[String *: String *: String *: Option[String] *: Option[
      String
    ] *: Option[String] *: Option[String] *: Arr[
      String
    ] *: Long *: EmptyTuple, Company] = sql"""
    UPDATE companies SET
            slug=$text,
            name=$text,
            url=$text,
            location=${text.opt},
            country=${text.opt},
            industry=${text.opt},
            image=${text.opt},
            tags=${_text} WHERE id=$int8
    """.query(companyDecoder)
// slug: String,
//     name: String,
//     url: String,
//     location: Option[String] = None,
//     country: Option[String] = None,
//     industry: Option[String] = None,
//     image: Option[String] = None,
//     tags: List[String] = List.empty
    postgres.use(
      _.prepare(query)
        .flatMap(
          _.unique(
            company.slug *: company.name *: company.url *: company.location *: company
              .country *: company.industry *: company.image *: Arr(
              company.tags: _*
            ) *: company.id *: EmptyTuple
          )
        )
    )
  }

  override def delete(id: Long): F[Company] = {
    val query = sql"""
    DELETE FROM companies WHERE id= $int8
    """.query(companyDecoder)
    postgres.use(_.prepare(query).flatMap(_.unique(id)))
  }

  override def getById(id: Long): F[Option[Company]] = {
    val query = sql"""
    SELECT * FROM comapnies WHERE id =$int8
    """".query(companyDecoder)

    postgres.use(_.prepare(query).flatMap(_.option(id)))
  }

  override def getBySlug(slug: String): F[Option[Company]] = {
    val query = sql"""
        SELECT * FROM comapnies WHERE slug =$text
        """".query(companyDecoder)

    postgres.use(_.prepare(query).flatMap(_.option(slug)))
  }

  override def getAll: F[List[Company]] = {
    val query: Query[Void, Company] = sql"""
    SELECT * FROM companies
    """.query(companyDecoder)

    postgres.use(_.execute(query))
  }

}

object CompanyRepositoryLive {

  def make[F[_]: Concurrent](
    postgres: Resource[F, Session[F]]
  ) = new CompanyRepositoryLive[F](postgres)

}
