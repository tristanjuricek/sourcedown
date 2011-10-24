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

import scala.xml.Node
import scalax.file._
import org.fusesource.scalate._

object TemplateFactory {
    
    def apply(fileInformation: Map[Path, Node], output: Path, subdir: Path,
              templatePath: String): CreateTemplate = {
      new ScalateTemplate(fileInformation, output, subdir, templatePath)
    }
}

trait CreateTemplate extends Function2[Path, Node, Unit] {
  def apply(path:Path, html: Node): Unit
}

class ScalateTemplate (
    val fileInformation: Map[Path, Node],
    val outputDir: Path,
    val root: Path,
    val templatePath: String)
    extends CreateTemplate{
  
  val engine = new TemplateEngine

  def apply(path: Path, html: Node) {
    val context = Map("path" -> path, 
                      "snippet" -> html,
                      "fileInformation" -> fileInformation)
    val content = engine.layout(templatePath, context)
    outputPath(path).write(content)
  }

  def outputPath(path: Path): Path = {
    val subdir = outputDir / path.parent.getOrElse(Path(".")).relativize(root)
    val outputPath = subdir / (path.name + ".html")
    outputPath
  }
}

