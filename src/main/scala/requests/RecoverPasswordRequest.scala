package requests

case class RecoverPasswordRequest(
    email: String,
    token: String,
    newPassword: String
)
