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

import scala.collection.mutable.{ Seq => MSeq, ListBuffer }
import scala.util.parsing.input.{ Reader, CharSequenceReader }

object CommentParser {
  
  def parse(sourcePath: SourcePath): Seq[Section] = {
    var codeStyle = CodeStyle.fromExtension(sourcePath.extension)
    parse(sourcePath, codeStyle)
  }

  /*
    Scans through the entire source file and tries to match it using the
    language comment style.

    This is actually the starting method that manages the resources in use.
  */
  def parse(sourcePath: SourcePath, codeStyle: CodeStyle): Seq[Section] = {
    
    // Read all characters in the stream
    // This will use the system's default encoding. Might be a problem.
    var content = 
      io.Source.fromFile(sourcePath.file).getLines().mkString("\n")

    var reader = new CharSequenceReader(content)

    var tokens = new ListBuffer[Section]

    readContent(codeStyle, reader, tokens, new ListBuffer[Char])

    return tokens
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

  private def readContent(codeStyle: CodeStyle, reader: Reader[Char], 
                          tokens: MSeq[Section], buffer: MSeq[Char]) {
    
    if (reader.atEnd) {
      tokens :+ Code(buffer)
      return
    }

    buffer :+ reader.first

    // TODO I don't think these side effects will work may have to put lines
    // into a subroutine

    val singleLine = for {
      start <- codeStyle.singleLineStart
      if (buffer.endsWith(start))
    } yield start;

    if (!singleLine.isEmpty) {
      var start = singleLine.head
      tokens :+ Code(buffer.dropRight(start.length))
      val comment = readComment("\n", reader.rest, tokens, ListBuffer[Char](start:_*))
      tokens :+ SingleLineComment(comment, start)      
    } 

    val multiMatches = for {
      (start, end) <- codeStyle.multiLineEnds
      if (buffer.endsWith(start))
    } yield (start, end)

    if (!multiMatches.isEmpty) {
      var (start, end) = multiMatches.head
      tokens :+ Code(buffer.dropRight(start.length))
      val comment = readComment(end, reader.rest, tokens, ListBuffer[Char](start:_*))
      tokens :+ MultiLineComment(comment, start, end)
    }

    readContent(codeStyle, if (reader.atEnd) reader else reader.rest, 
          tokens, buffer)            
  }

  implicit def toString(seq: Seq[Char]): String =
    new String(seq.toArray)

  private def readComment(end: String, reader: Reader[Char], 
              tokens: MSeq[Section], buffer: MSeq[Char]) : String = {
    if (reader.atEnd) {
      return buffer
    }

    buffer :+ reader.first

    if (buffer.endsWith(end)) {
      return buffer
    }

    readComment(end, reader.rest, tokens, buffer)
  }
}
