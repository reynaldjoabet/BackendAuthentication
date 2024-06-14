package requests

final case class DeleteAccountRequest(
  email: String,
  password: String
)
