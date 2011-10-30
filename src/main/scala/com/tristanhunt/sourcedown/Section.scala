/*
  # The Parse Tree of Source Files

  For this version, it's outrageously simple.
*/
package com.tristanhunt.sourcedown

trait Section {
  /*
    Source is actually the comment line including any markers
  */
  def source: String

  /*
    Return the source minus any markers.
  */
  def trim: String = source
}

case class Code(val source: String) extends Section

/*
  The source of a single line comment is terminated by a newline. In cases that
  it's not, you'd have a multiline comment.
*/
case class SingleLineComment(val source: String, val start: String) 
  extends Section {

  override def trim = source.substring(start.length).trim
}

case class SingleLineGroup(val comments: Seq[SingleLineComment]) 
extends Section {
  
  def source = comments.mkString("")

  override def trim = comments.map( x => x.trim ).mkString("\n")
}

case class MultiLineComment(val source: String, val start: String, 
                            val end: String) extends Section {

  // TODO this just feels wrong, but I'm not sure where to stick it.
  var tabWidth = 4

  /*
    With a multiline comment you need to try to figure out where the "real"
    content starts in the comment. 
    
    Keep in mind the tendency to see strings like "*" preceding content like
    you see in java:
    
      /**
       * I am a comment
       */
    
    Here, what we want as content is just the "I am a comment\n" bit.

    Another example:

      <!-- Multiline possible comment -->

    This should be "Multiline possible comment". As a general rule, we'll
    probably ensure each rebased block ends with two newlines.
    
    Tabs are problematic. For some kind of sanity, we'll replace the tab 
    width using a general policy (which is probably true across all files in
    the project).
  */
  override def trim: String = {
    var startIndex = start.length
    if (start == "/*" && source.startsWith("/**")) {
      startIndex += 1
    }
    var lines = source.substring(startIndex, source.length - end.length)
              .replace("\t", " " * tabWidth /* FIXME */)
              .split("\n")
    if (lines.length > 2) {
      // Drop the leading and trailing line if they don't have much 
      // interesting content, which is generally true.
      if (lines.head.trim.isEmpty)
        lines = lines.drop(1)
      if (lines.last.trim.isEmpty)
        lines = lines.dropRight(1)
      
      // Look at the rest of the content to see if we notice a prefix.
      findPrefix(lines, start).foreach { prefix =>
        lines = lines.map( line => line.drop(prefix.length) )
      }
    }
    return lines.map(_.trim).mkString("\n") + "\n\n"
  }

  /*
    If there is more than one line to read, we'll try to discover what the
    prefix might be, since people can do things like this:

      <!--
         - A two-line 
         - multiline comment
        -->
    
    Keep in mind, however, the tendency for using multline comments using
    one of the characters wihtin the starting sequence. If there's only
    one line, we'll start with a guess using the first character that
    is not in the starting sequence.

    If we don't discover something, we'll return `None`. Otherwise it will
    be `Some(prefix)`.
  */
  private def findPrefix(lines: Seq[String], start: String): Option[String] = {
    lines.find( stringIsntEmpty _ ).flatMap { line =>
      val idx = line.indexWhere( charIsNotIn(start) )
      if (idx > -1) 
        Some(line.substring(0, idx))
      else
        None
    }
  }

  private def charIsNotIn(str: String): Char => Boolean = 
    ch => !ch.isWhitespace && !str.contains(ch)

  private def stringIsntEmpty(str: String): Boolean =
    !str.trim.isEmpty
}


