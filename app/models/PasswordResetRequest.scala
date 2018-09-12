package models

import java.sql.Timestamp

case class PasswordResetRequest(username:String,
                                uuid:String,
                                resetPasswordDate:Timestamp)