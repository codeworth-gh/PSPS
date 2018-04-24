package models

case class User(id:Long,
                 username:String,
                 name:String,
                 email:String,
                 encryptedPassword:String)