/*

Any of the code will be run through a "syntax highlighter" which is assumed to
take the source code snippet and spit out some HTML.



*/
package com.tristanhunt.sourcedown

import java.io._
import scala.sys.process._
import scala.xml._
import scalax.file._

object Highlight extends Highlight


object Pygmentize extends Function2[String,String,String] {
  
  def apply(ext: String, source: String): String = {
    Process("pygmentize -f html -l " + ext ) #< asStream(source) !!
 }

  def asStream(source: String): InputStream = {
    new ByteArrayInputStream( source.getBytes )
  }
}


class Highlight {
  
  lazy val tryPygmentize: Option[(String, String) => String] = {
    try {
      Pygmentize("js", "function() {}")
      Some(Pygmentize)
    } catch {
      case _ => 
        println("pygmentize not found, no syntax highlighting will be applied. See http://pygments.org.")
        None
    }
  }

  def apply(source: String, path: Path): String = {
    var ext = path.extension.getOrElse("unknown")
    try {
      val start = System.currentTimeMillis
      print("Highlighting " + path)
      val converted =
        tryPygmentize.map( pygmentize => pygmentize(ext, source) )
                     .getOrElse( default(source, path) )      
      println("done in " + (System.currentTimeMillis - start) + "ms")
      converted
    } catch {
      case _ => default(source, path)
    }
  }

  def default(source: String, path: Path): String = {
    <pre class={path.extension.map("brush: " + _).getOrElse("")}>{source}</pre>
  }

  // Tristan says I'm not entirely sure about this. TODO investigate
  implicit private def toString(node: Node): String = {
    xml.Utility.toXML(node).toString
  }
}
