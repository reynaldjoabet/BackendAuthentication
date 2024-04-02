package domain

final case class UserID(id: Long, email: String,roles: Set[Role])
