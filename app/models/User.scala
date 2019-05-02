package models

import com.mohiva.play.silhouette.api.Identity

case class User(id:Long,
                 username:String,
                 name:String,
                 email:String,
                 encryptedPassword:String) extends Identity