package models

case object UserRole extends Enumeration {
  /**
    * A site administrator. Can edir and delete other users.
    */
  val Admin = Value
  
  // A sample role
  val SampleRole1 = Value
  val SampleRole2 = Value
}

case class User(id:Long,
                 username:String,
                 name:String,
                 email:String,
                 encryptedPassword:String,
                 roles:Set[UserRole.Value]
               ){
  def isAdmin:Boolean = roles(UserRole.Admin)
  def otherRoles:Set[UserRole.Value] = roles.filter(_!=UserRole.Admin)
}