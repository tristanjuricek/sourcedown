/*
  # Templating

  Templating is the act of taking a bunch of html snippets, based on random
  code files, and pushing all that context to a template engine to do it's
  thing.

  This, by default, will use a scalate based system to iterate over all the
  different HTML files and use each one to build up web pages, one for each
  source file.

  By assigning a different template function to the Templating object, you
  can customize the system in a completely different way. Or just tweak how
  the ScalateTemplating system works by pointing to your own resources.

  ## object Templating

  ### Methods

  * `run(sourceTree: SourceTree, resourcePaths: Seq[Resource])`
    The method that executes the template function (pretty simple).
  
  ### Properties 

  * `template: Function[SourceTree, Unit]` 
    A property that you can override for custom template stuff.

  * `createContext: 
      (markdown: String, file: SourceTree.File, sourceTree: SourceTree) 
          => Map[String, Object]`
   Create the rendering context to be used on all templates. By default, we
   create a map of the arguments.      

  ## object ScalateTemplating

  * `run(sourceTree)` 
    The default implementation that uses classpath resources compiled into
    the sytem.

  ### Properties

  * `engine`
    The scalate template engine.
  
  * `templateFile`
    The template file to use to generate the template. This will be called
    using the default map context method.
*/

package com.tristanhunt.sourcedown

import scalax.file._
import org.fusesource.scalate._

object TemplateFactory {
    
    def apply(fileInformation: Map[Path, String], output: Path, root: Path,
              templatePath: String): CreateTemplate = {
      new ScalateTemplate(fileInformation, output, root, templatePath)
    }
}

// This is just to remove the ScalateTemplate reference in the client.
trait CreateTemplate extends Function2[Path, String, Unit] {
  def apply(path:Path, html: String): Unit
}

class ScalateTemplate (
    val fileInformation: Map[Path, String],
    val outputDir: Path,
    val root: Path,
    val templatePath: String)
    extends CreateTemplate{
  
  val engine = new TemplateEngine

  def apply(path: Path, html: String) {
    val context = Map("path" -> path, 
                      "snippet" -> html,
                      "toRoot" -> toRoot(path),
                      "navList" -> navListFor(path))
    val content = engine.layout(templatePath, context)
    outputPath(path).write(content)
  }

  def outputPath(path: Path): Path = {
    Path( (outputDir / subpath(path)).path + ".html" )
  }

  def toRoot(path: Path): String = {

    path.parent match {
      
      case None => "."

      case Some(parent) => 
        val toRoot =
          subpath(parent).segments.filter( x => !x.isEmpty ).map( x =>  ".." ).mkString("/")
        if (toRoot.isEmpty) "." else toRoot
    }
  }

  /* If we don't remove the . we have null flying around unabashed */
  def absoluteOf(p: Path): Path = {
    val absoluted:Path = p.toAbsolute
    val start = absoluted.root.map(x => x.path).getOrElse("")
    Path(start + absoluted.segments.filter(x => x != ".").mkString("/"))
  }

  def subpath(path: Path): Path = {
    absoluteOf(path).relativize(absoluteOf(root))
  }

  /*
    Convert file information into a navigation list.
  */
  private def navListFor(path: Path): Seq[(String, String, Boolean)] = {
    // The start of any link to any file is a big relative path statement.
    val toRootString = toRoot(path)

    val iter =
      for {
        (p1, content) <- fileInformation
        subPath = subpath(p1).path
        link = toRootString + "/" + subPath + ".html"
        isActive = path == p1
      } yield (subPath, link, isActive)

    iter.toSeq
  }
}

