package models

import java.sql.Timestamp

case class UuidForForgotPassword (username:String,
                                  uuid:String,
                                  resetPasswordDate:Timestamp)