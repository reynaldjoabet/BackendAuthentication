package domain

case class User(id: Long, username: String, roles: Set[String])
