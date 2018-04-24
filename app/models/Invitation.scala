package models
import java.sql.Timestamp


case class Invitation (email:String,
                       date:Timestamp,
                       uuid:String,
                       sender:String)
