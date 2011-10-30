/*
  This is all of the "data" that we need to model in the application. It's
  broken down into the major parts:

  1. What we know about a particular source file path. It's probable language,
     etc.
  2. The "code style" or language format of a particular file
  3. The very basic "parse" information of comments and non-comments in a 
     source file.


  ## Class SourcePath

  ### Properties

  * `file: File`
  * `relative: Seq[String]`
  * `extension: String`


  ## CodeStyle 

  These are a collection of case objects that match different langauges

  * `CStyle`
  * `ShellStyle`
  * `XMLStyle`

  ### Properties

  * `singleLineStart: Set[String]`
  * `multiLineEnds: Set[(String, String)]`


  ## Section

  Two kinds of sections

  * `Code(source: String)`
  * `SingleLineComment(source: String, start: String)`
  * `MultiLineComment(source: String, start: String, end: String)`


  ## class SourceDirectory / SourceFile

  Provides an access API for all source files and generated stages. This 
  may be stored in a filesystem, a DB, or just in memory.

  This is an unbalanced tree where each node represents a file or directory,
  just like a filesystem.



  ## trait Resource

  Provides a URL-like abstraction to getting file contents from a file path
  or the classpath.

*/

package com.tristanhunt.sourcedown


/*  
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
  def singleLineStart: Set[String]
  def multiLineEnds: Set[(String, String)]
}

case object CStyle extends CodeStyle {
  val singleLineStart = Set("//")
  val multiLineEnds = Set( ("/*", "*/") )
}

case object ShellStyle extends CodeStyle {
  val singleLineStart = Set("#")
  val multiLineEnds:Set[(String, String)] = Set.empty
}

case object XMLStyle extends CodeStyle {
  val singleLineStart:Set[String] = Set.empty
  val multiLineEnds = Set( ("<!--", "-->") )    
}