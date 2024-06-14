package domain

final case class PasswordRecoveryToken(
  email: String,
  token: String,
  expiration: Long
)
