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

class Sourcedown(val root: String, val ignoreSubs: Seq[String], val output: String) {

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
    var ignoreFolders:ListBuffer[String] = ListBuffer.empty

    val iter = args.iterator
    while (iter.hasNext) {
      iter.next match {
        case "-r" => check(iter)( () => { root = iter.next } )
        case "-i" => check(iter)( () => { ignoreFolders :+ iter.next } )
        case str:String => output = str
      }
    }

    createSourcedown(root, ignoreFolders, output).run
  }

  private def check(iter: Iterator[String])(fxn: () => Unit) {
    if (iter.hasNext)
      fxn()
    else {
      printOptions
      sys.error("Invalid argument sequence, one of the required values is missing")
    }
  }

  /*
    This is the short option you see when executing a help option. Probably
    should not be any kind of replacement for actual online help.
  */
  def printOptions {
    var helpString = """
      |Usage: java -jar sourcedown.jar [-h] [-r DIR] [-i DIR1] [-i DIR2] OUTPUT
      |
      |Options:
      |
      |   -i DIR      Ignore a particular folder pattern. This will be
      |               applied from the beginning of the string. Ergo,
      |               "src/main" will ignore "src/main/sh/File.sh" but not
      |               "foo/src/main/sh/File.sh". Can be repeated.
      |
      |   -r DIR      Set the "root" of the project. Default is the current 
      |               working directory.
      |
      |   -h          Print usage.
      |""".trim.stripMargin
    System.err.println(helpString);
  }

  def createSourcedown(root: String, ignoreSubs: Seq[String], output: String): Sourcedown = {
    return new Sourcedown(root, ignoreSubs, output);
  }
}

