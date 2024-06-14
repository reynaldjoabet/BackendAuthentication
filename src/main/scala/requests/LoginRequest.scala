package requests

final case class LoginRequest(
  email: String,
  password: String
)
