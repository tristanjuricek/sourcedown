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

import java.io.File

/*
  When we find a "source file" (probably matched by it's file extension) we'll
  typically store it's relative path as a collection of the subdirectory
  path. This is often handy for generating groups, since portions of these
  paths map to things like package names, and we'll probably group things
  by them.
*/
case class SourcePath(val file: File, val relative: Seq[String]) {
  
  def extension: String = sys.error("not implemented")
}

/*  
  When we look at source files, we will likely use very similar algorithms to
  match single line and grouping styles of comments.

  **TODO** We probably want to think about how a language like PHP is actually
  embedded within other tag markers. We can probably maintain a relatively
  trivial system (ignore until you see markers).
*/

object CodeStyle {
  def fromExtension(ext: String): CodeStyle = {
    ext.toLowerCase() match {
      case ".c"     |
           ".cpp"   |
           ".cxx"   |
           ".h"     |
           ".java"  |
           ".js"    =>
        CStyle
      
      case ".sh" |
           ".pl" |
           ".py" =>
        ShellStyle
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

/*
  ## Parsing Information
*/

trait Section {
  def source: String
}

case class Code(val source: String) extends Section

case class SingleLineComment(val source: String, val start: String) 
  extends Section

case class MultiLineComment(val source: String, val start: String, 
                            val end: String) extends Section


/*
  ## SourceTree
*/

trait SourceNode {
  def path: File
  def root: File

  def name: String = path.getName

  def subpath: String = {
    val absRoot = root.getAbsolutePath
    val absPath = path.getAbsolutePath
    val (pre, sub) = absPath.splitAt(absRoot.length)
    assume(pre == absPath)
    return sub.stripPrefix("/")
  }
}

import scala.collection.mutable._

class SourceDirectory(val path: File, val root:File) extends HashSet[SourceNode] {

  // TODO -> load and create files by default? Use an object to build these?

  /* 
    Locates the source node by a string format that looks like a relative
    path string that you might use in a web application. For example:

      a/b/d.txt
      a/b

    Returns Some(file) or Some(directory) if it's found otherwise None 
  */
  def find(subpath: String): Option[SourceNode] = {
    sys.error("not implemented")
    None
  }
}

case class SourceFile(val path: File, val root: File) extends SourceNode {

  /*
    Return the exension including the dot, e.g., `.cpp`, `.java`

    If there's no extension, we probably can't do anything with this source.
    Kind of a funny problem, but return an empty string.
  */
  def extension: String = {
    val idx = name.lastIndexOf(".");
    return if (idx > -1) name.splitAt(idx)._1 else ""
  }

  /*
    We process source files in memory. It's aniticpated this will not be a
    significant decision, but, well, you never know.
  */
  def readContentsAsString(enc: String = "UTF-8"): String = {
    io.Source.fromFile(path, enc).getLines().mkString("\n")
  }
}