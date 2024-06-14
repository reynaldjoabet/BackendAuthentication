package requests

final case class CreateReviewRequest(
  companyId: Long,
  management: Int,
  culture: Int,
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String
)
