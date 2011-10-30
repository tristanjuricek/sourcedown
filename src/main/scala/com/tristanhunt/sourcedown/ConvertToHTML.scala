/*

# ConvertToHTML

(In progress)

This should do the general 2-column layout thing, basically as one big fat
table. (Kind of old-school, but hey, we're programmers!)

*/

package com.tristanhunt.sourcedown

import scalax.file._
import scala.xml._
import scala.collection.mutable.ListBuffer

object ConvertToHTML extends ConvertToHTML {
}

class ConvertToHTML {
  
  /*
    TODO -> we need to consider the index of the code in question.

  */
  def apply(path: Path, sections: Seq[Section]): String = {
    
    var sb = new StringBuilder

    // Convert each section, then place each converted part in a "left or right"
    // pairing.

    val pairs = new ListBuffer[(String,String)]

    // Whenever we get a new left, we always add a pair.
    def addLeft(x: String) {
      pairs.append( (x, "") )
    }

    // Try to replace the right value of the last pair. If you can't, add a new
    // pair with an empty left.
    def addRight(x: String) {
      if (!pairs.isEmpty && pairs.last._2.isEmpty) {
        var old = pairs.remove(pairs.length - 1);
        pairs.append( (old._1, x) )
      } else {
        pairs.append( ("", x) )
      }
    }

    sections.foreach { s => s match {
      
      case Code(source) =>
        addRight(Highlight(source, path))
      
      case group:SingleLineGroup =>
        addLeft(ConvertMarkdown(group.trim))

      case line:SingleLineComment =>
        addLeft(ConvertMarkdown(line.trim))

      case multi:MultiLineComment =>
        addLeft(ConvertMarkdown(multi.trim))
    }}

    pairs.map { case (x, y) => "<tr><td>" + x + "</td>" +
                                   "<td>" + y + "</td></tr>" }
         .mkString("")
  }

  // Tristan says I'm not entirely sure about this. TODO investigate
  implicit private def toString(node: Node): String = {
    xml.Utility.toXML(node).toString
  }
}