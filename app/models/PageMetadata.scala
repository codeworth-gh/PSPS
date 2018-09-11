package models


case class PageMetadata(
                         canonicalUrl:String,
                         title:String,
                         description:String,
                         summary:String,
                         keywords:Set[String],
                         imageUrl:Option[String],
                         twitterHandle:Option[String]
                       ) {
  def getSummary = if(summary==null || summary.trim.isEmpty) description else summary
  
}

object PageMetadata {
  
  def apply(title:String, description:String, keywords:Set[String]):PageMetadata = apply(null, title, description, keywords)
  
  def apply( canonicalUrl:String, title:String, description:String, keywords:Set[String] ):PageMetadata = new PageMetadata(
    canonicalUrl, title, description, description,
    if (keywords==null||keywords.isEmpty) title.split(" ").toSet else keywords,
    None, None)
  
  /**
    * An empty instance, for when we have nothing to say about the page.
    */
  val empty = PageMetadata(null,null,null,null,Set(),None,None)
}
