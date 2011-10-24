/*
  # Rebasing

  Before converting the entire file into an HTML fragment, this will deal with
  whitespace.

  ## class Rebaser

  Note that there is an `object Rebaser` which maintains a single instance
  for custom styling of the code, for example.

  ### Methods

  * `rebase(sections, [sourceClass]): String` Converts the sequence of parsed 
    source sections into a single markdown file. You can set up extra class
    information for each of the Code sections.

*/

package com.tristanhunt.sourcedown

object Rebase {

  var default = new Rebase

  def apply(sections: Seq[Section], sourceClass: String = null): String =
    default.apply(sections, sourceClass)
}

class Rebase {
  
  var tabWidth = 4

  def apply(sections: Seq[Section], sourceClass: String): String = {
    sections.map(toMarkdown(_, sourceClass)).mkString("\n")
  }

  private def toMarkdown(section: Section, sourceClass: String): String = {
    section match {
      case Code(source) => wrapSource(source, sourceClass)
      case SingleLineComment(source, start) => 
        convertSingleLineComment(source, start)
      case MultiLineComment(source, start, end) =>
        stripMultiLineComment(source, start, end)
    }
  }

  /*
    We need to not only wrap the source code, but also make sure it's 
    entitized properly here. So we'll actually used the embedded XML stuff
    and then write that out.
     
    So the following code: 
     
      if (4 < 3) {
        println("4 < 3");
      }

    It should generally become:
    
      <code><pre>if (4 &lt; 3) {
        println("4 &lt; 3");
      }</pre></code>       
    
    (With an extra output newline for fun.)

    An additional 'class hint' is passed through. Set to null to disable the
    class attribute from being applied.
  */
  private def wrapSource(source: String, sourceClass: String) : String = {
    // TODO this seems buggy, I don't see the source class and why do I need to 
    // trim?
    val wrapped =
      <pre><code>{source}</code></pre>
//      <pre><code class={sourceClass}>{source}</code></pre>
    xml.Utility.toXML(wrapped).toString
  }

  private def convertSingleLineComment(source: String, start: String): String = {
    "\n" + source.substring(start.length).trim + "\n"
  }

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
  private def stripMultiLineComment(source: String, start: String, 
                    end: String): String = {
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