package requests

final case class UpdatePasswordRequest(
    email: String,
    oldPassword: String,
    newPassword: String
)
