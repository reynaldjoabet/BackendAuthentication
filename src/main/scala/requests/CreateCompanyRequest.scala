package requests

import domain._

final case class CreateCompanyRequest(
  name: String,
  url: String,
  location: Option[String] = None,
  country: Option[String] = None,
  industry: Option[String] = None,
  image: Option[String] = None,
  tags: Option[List[String]] = None
) {

  def toCompany(id: Long): Company =
    Company(
      id,
      name,
      name,
      url,
      location,
      country,
      industry,
      image,
      tags.getOrElse(List.empty)
    )

}
