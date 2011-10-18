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

import org.fusesource.scalate._

object Templating {
    
    def run(sourceTree: SourceDirectory) {
        template(sourceTree)
    }

    var template: SourceDirectory => Unit = ScalateTemplating.run

    // TODO there's probably a shortcut here
    var createContext: (String, SourceFile, SourceDirectory) => Map[String, Object] =
        (markdown, file, sourceTree) => Map("markdown" -> markdown, 
                                            "file" -> file, 
                                            "sourceTree" -> sourceTree)

    var save: (String, SourceFile) => Unit = {
      (markdown, file) => {
        sys.error("not implemented")
      }
    }
}

object ScalateTemplating {

    val engine = new TemplateEngine
    
    def run(sourceTree: SourceDirectory) {
      sys.error("not implemented")
      //sourceTree.foreach( n => Templating.save( template(s, sourceTree), file) )
    }

    def template(sourceFile: SourceNode, root:SourceDirectory):String = {
      sys.error("not implemented")
      ""
    }
}