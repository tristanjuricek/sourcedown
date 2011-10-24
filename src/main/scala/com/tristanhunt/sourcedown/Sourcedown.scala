/*
  ## Class Sourcedown

  This is expected to be most of the API for people using the tool.

  This class handles a lot of the convention decisions. Like, we're going to
  just find all sources, but these sources are going to use an XHTML writing
  system.
  
  ### Properties

  - `root` The directory path we start searching.
  - `ignoreSubs` As we navigate, we'll ignore the files that start with 
    these directories. Typically stated as relative to the root directory.
  - `output` The output directory tree. Might store temporary stuff too.
  
  ### Methods

  - `run` Executes the conversion process. 

  ## Object Sourcedown

  This is the app entry point and a factory method.

  ### Methods

  - `main(args)` The app entry point
  - `createSourcedown(root, ingoreSubs): Sourcedown` 
*/

package com.tristanhunt.sourcedown

import scala.collection.mutable.ListBuffer
import scalax.file._
import scalax.io._
import scalax.file.PathMatcher._
import scalax.file.PathMatcher.StandardSyntax.REGEX
import java.util.regex.Pattern.quote

class Sourcedown(
  val dir: String, 
  val onlySubs: Seq[String], 
  val output: String,
  val templatePath: String,
  val resources: Seq[String]) {

  /*
    Sourcedown:

    1. Comment parsing
    2. Rebasing
    3. Conversion
    4. Templatization
    5. Site completion

    This generally is just going to run everything in the same thread
    right now. This allows the template to reference a tree of stuff.
   */
  def run {
    val root = Path(dir)

    val subs = if (onlySubs.isEmpty) Seq(".") else onlySubs

    var fileIterator = 
      for {
        subdir <- subs.map(root / _)
        file <- subdir.descendants(IsFile)
        sections = ParseComments(file)
        markdown = Rebase(sections)
        html = ConvertMarkdown(markdown)
      } yield (file, html);
    
    var pathToHTML = Map(fileIterator.toSeq: _*)

    var applyTemplate = TemplateFactory(pathToHTML, output, root, templatePath)

    fileIterator.foreach {
      case (x,y) => applyTemplate.apply(x,y)
    }

    // Copy resources to a "output/res" folder
    resources.foreach( copyToRes )
  }

  private def createMatcher(regex: String): PathMatcher = {
    return scalax.file.FileSystem.default.matcher(regex, REGEX) 
  }

  private def copyToRes(input: String) {
    val resource = Resource.fromClasspath( input )
    val outputPath = Path(output) / "res" / Path(input).name
    outputPath.write( resource.chars )
  }
}

object Sourcedown {
  
  /**
    This function handles the command line parsing, then we push everything
    into the sourcedown instance.

    See
  */
  def main(args: Array[String]) {
    
    if (args.contains("-h")) {
      printOptions
      return
    }

    var output:String = ""
    var root:String = "."
    var templatePath:String = "com/tristanhunt/sourcedown/template.ssp"
    var onlySubs:ListBuffer[String] = ListBuffer.empty
    var resources:ListBuffer[String] = ListBuffer(
      "com/tristanhunt/sourcedown/res/style.css")

    val iter = args.iterator
    while (iter.hasNext) {
      iter.next match {
        case "-d" => check(iter)( () => { root = iter.next } )
        case "-s" => check(iter)( () => { onlySubs += iter.next } )
        case "-o" => check(iter)( () => { output = iter.next } )
        case str:String => println("ignoring " + str)
      }
    }

    createSourcedown(root, onlySubs, output, templatePath, resources).run
  }

  private def check(iter: Iterator[String])(fxn: () => Unit) {
    if (iter.hasNext)
      fxn()
    else {
      printOptions
      sys.error("Missing a parameter after one of the options")
    }
  }

  /*
    This is the short option you see when executing a help option. Probably
    should not be any kind of replacement for actual online help.
  */
  def printOptions {
    var helpString = """
      |Usage: java -jar sourcedown.jar [-h] [-d DIR] [-s SUB] [-t TEMPLATE] OUTPUT
      |
      |Options:
      |
      |   -s DIR      Only include files from this subdirectory. Can be repeated.
      |
      |   -d DIR      Set the "root" of the project. Default is the current 
      |               working directory.
      |
      |   -h          Print usage.
      |""".trim.stripMargin
    System.err.println(helpString);
  }

  def createSourcedown(root: String, ignoreSubs: Seq[String], 
                       output: String, templatePath: String,
                       resources: Seq[String]): Sourcedown = {
    return new Sourcedown(root, ignoreSubs, output, templatePath, resources);
  }
}

