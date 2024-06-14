package domain

final case class Company(
  id: Long,
  slug: String,
  name: String,
  url: String,
  location: Option[String] = None,
  country: Option[String] = None,
  industry: Option[String] = None,
  image: Option[String] = None,
  tags: List[String] = List.empty[String]
)
