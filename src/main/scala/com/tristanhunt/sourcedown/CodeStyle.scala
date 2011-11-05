/*
  Declares a fairly limited set of parameters which helps drive the ParseComments
  mechanism.
*/

package com.tristanhunt.sourcedown


/*  
 ## CodeStyle 

  These are a collection of case objects that match different langauges

  * `CStyle`
  * `ShellStyle`
  * `XMLStyle`

  When we look at source files, we will likely use very similar algorithms to
  match single line and grouping styles of comments.

  **TODO** We probably want to think about how a language like PHP is actually
  embedded within other tag markers. We can probably maintain a relatively
  trivial system (ignore until you see markers).
*/

object CodeStyle {
  def apply(ext: String): Option[CodeStyle] = {
    ext.toLowerCase() match {
      case "c"     |
           "cpp"   |
           "cxx"   |
           "h"     |
           "java"  |
           "scala" |
           "js"    =>
        Some(CStyle)
      
      case "sh" |
           "pl" |
           "py" =>
        Some(ShellStyle)
      
      case "xml" |
           "html" |
           "xhtml" |
           "xsl" =>
        Some(XMLStyle)
      
      case _ => None
    }
  }
}

trait CodeStyle {
  /*
    Most languages have a simple character sequence to start a single comment
    line.
  */
  def singleLineStart: Set[String]

  /*
    Start and end markers for comments.
  */
  def multiLineEnds: Set[(String, String)]

  /*
    Strings and text are usually indicated via a single or double quote, with
    a particular group of escape characters.
  */
  def stringDelimiters: Set[(String, String, Set[String])]
}

case object CStyle extends CodeStyle {
  val singleLineStart = Set("//")
  val multiLineEnds = Set( ("/*", "*/") )
  val stringDelimiters = Set( ("\"", "\"", Set("\\")) )
}

case object ShellStyle extends CodeStyle {
  val singleLineStart = Set("#")
  val multiLineEnds:Set[(String, String)] = Set.empty
  val stringDelimiters = Set( ("'", "'", Set("\\")), ("\"", "\"", Set("\\")) )
}

case object XMLStyle extends CodeStyle {
  val singleLineStart:Set[String] = Set.empty
  val multiLineEnds = Set( ("<!--", "-->") )
  val stringDelimiters = Set( ("'", "'", Set("\\")), ("\"", "\"", Set("\\")) )
}