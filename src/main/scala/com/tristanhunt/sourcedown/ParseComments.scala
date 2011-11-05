/*
  # Parsing in Sourcedown

  ***TODO*** This needs to ignore strings. (duh)

  "Start here" to know about parsing in Souredown. 

  Generally speaking, we only care about comments in sourcedown. The
  trick is that we care about them in lots of different languages.

  This takes what we know about a file (typically it's path) and then we 
  find the suitable parser for the language. What we get out is a stream of
  comments or code. Code is defined as "not a comment". 

  ## Class CommentParser

  The top-level methods that basically go from "file in filesystem" to input
  file.

  * `parse(sourcePath): Seq[Section]`
  * `parse(sourcePath, codeStyle): Seq[Section]`

*/

package com.tristanhunt.sourcedown

import scala.collection.mutable.{ ListBuffer }
import scala.util.parsing.input.{ Reader, CharSequenceReader }
import scalax.file._

object ParseComments {
  
  def apply(path: Path): Seq[Section] =
    apply(path, CodeStyle(path.extension.getOrElse("unknown")))

  /*
    Scans through the entire source file and tries to match it using the
    language comment style.

    This is actually the starting method that manages the resources in use.
  */
  def apply(path: Path, styleOpt: Option[CodeStyle]): Seq[Section] = {

    // Read all characters in the stream
    // This will use the system's default encoding. Might be a problem.
    var content = path.lines(includeTerminator = true)
                      .mkString("")

    styleOpt match {
      
      case None =>
        Seq(Code(content))

      case Some(codeStyle) =>

        var reader = new CharSequenceReader(content)
        var tokens = new ListBuffer[Section]

        readContent(codeStyle, reader, tokens, new ListBuffer[Char])

        return createGroups(tokens.filter( x => !x.source.matches("\\s*") ))
    }
  }

  /*
    These methods depend upon other methods that basically consume a reader
    and append data to input parameters:

    * `readContent(codeStyle, reader, tokens, buffer)` - Assumes we are not 
      in a comment
    * `readComment(end, reader, tokens, buffer)` - Reads until the end 
      marker is found

    TODO right now this might not handle php particularly well, but we'll
    just have to see.        


  */

  def readContent(codeStyle: CodeStyle, reader: Reader[Char], 
                  tokens: ListBuffer[Section], buffer: ListBuffer[Char]) {

    var pos = reader

    if (reader.atEnd) {
      tokens += Code(buffer)
      return
    }

    buffer += pos.first

    val string = for {
      (start, end, escape) <- codeStyle.stringDelimiters
      if (buffer.endsWith(start))
    } yield (start, end, escape)

    if (!string.isEmpty) {
      var (start, end, escape) = string.head
      val nextPos = readString(end, escape, reader.rest, tokens, buffer)
      readContent(codeStyle, nextPos, tokens, buffer)
      return
    }

    val singleLine = for {
      start <- codeStyle.singleLineStart
      if (buffer.endsWith(start))
    } yield start;

    if (!singleLine.isEmpty) {
      var start = singleLine.head
      val content = buffer.dropRight(start.length)
      if (!content.isEmpty)
        tokens += Code(content)
      buffer.clear
      val (comment, nextPos) = 
        readComment("\n", pos.rest, tokens, ListBuffer[Char](start:_*), codeStyle)
      tokens += SingleLineComment(comment, start)
      readContent(codeStyle, nextPos, tokens, buffer)   
      return
    } 

    val multiMatches = for {
      (start, end) <- codeStyle.multiLineEnds
      if (buffer.endsWith(start))
    } yield (start, end)

    if (!multiMatches.isEmpty) {
      var (start, end) = multiMatches.head
      val content = buffer.dropRight(start.length)
      if (!content.isEmpty)
        tokens += Code(content)
      buffer.clear
      val (comment, nextPos) = 
        readComment(end, pos.rest, tokens, ListBuffer[Char](start:_*), codeStyle)
      tokens += MultiLineComment(comment, start, end)
      readContent(codeStyle, nextPos, tokens, buffer)
      return
    }

    readContent(codeStyle, if (pos.atEnd) pos else pos.rest, 
          tokens, buffer)
  }

  implicit def toString(seq: Seq[Char]): String =
    new String(seq.toArray)

  private def readComment(end: String, reader: Reader[Char], 
              tokens: ListBuffer[Section], buffer: ListBuffer[Char],
              codeStyle: CodeStyle) : (String, Reader[Char]) = {
    if (reader.atEnd) {
      return (buffer, reader)
    }

    buffer += reader.first

    var nextPos = reader.rest

    val string = for {
      (start, end, escape) <- codeStyle.stringDelimiters
      if (buffer.endsWith(start))
    } yield (start, end, escape)

    if (!string.isEmpty) {
      var (start, end, escape) = string.head
      nextPos = readString(end, escape, reader.rest, tokens, buffer)
    }

    if (buffer.endsWith(end)) {
      return (buffer, reader.rest)
    }

    readComment(end, nextPos, tokens, buffer, codeStyle)
  }

  /*
    Combines sequences of single line comments. We ditch sequences of whitespace
    only code blocks in between.
  */
  private def createGroups(in: Seq[Section]) : Seq[Section] = {

    val buffer = new ListBuffer[Section]
    var group = new ListBuffer[SingleLineComment]

    def continue(s: Section): Unit = {
      if (!group.isEmpty) {
        buffer += SingleLineGroup( group )
        group = new ListBuffer[SingleLineComment]
      }
      buffer += s
    }

    (0 until in.length).foreach { index =>
      in(index) match {
        case s:SingleLineComment =>
          group += s

        case c:Code =>             continue(c)
        case m:MultiLineComment => continue(m)
      }
    }

    if (!group.isEmpty) buffer += SingleLineGroup( group )

    return buffer
  }

  private def isSingleLineAt(in: Seq[Section], index: Int): Boolean = {
    
    var isSingle = false

    if (in isDefinedAt index) {
      in(index) match {
        case s:SingleLineComment => isSingle = true
        case _ =>                   isSingle = false
      }
    }
    
    return isSingle
  }

  private def readString(end: String, escape: Set[String], 
                         reader: Reader[Char],
                         tokens: ListBuffer[Section], 
                         buffer: ListBuffer[Char]): Reader[Char] = {
      if (reader.atEnd) {
        return reader
      }

      buffer += reader.first

      if (buffer.endsWith(end)) {
        if ( escape.find( x => buffer.endsWith(x + end)).isEmpty ) {
          return reader.rest          
        }
      }

      readString(end, escape, reader.rest, tokens, buffer)
  }
}
