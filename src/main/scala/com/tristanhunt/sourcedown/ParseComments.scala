/*
  # Parsing in Sourcedown

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

        tokens
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
        readComment("\n", pos.rest, tokens, ListBuffer[Char](start:_*))
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
        readComment(end, pos.rest, tokens, ListBuffer[Char](start:_*))
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
              tokens: ListBuffer[Section], buffer: ListBuffer[Char]) : (String, Reader[Char]) = {
    if (reader.atEnd) {
      return (buffer, reader)
    }

    buffer += reader.first

    if (buffer.endsWith(end)) {
      return (buffer, reader.rest)
    }

    readComment(end, reader.rest, tokens, buffer)
  }
}