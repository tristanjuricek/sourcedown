package com.tristanhunt.sourcedown

import org.scalatest.Spec
import scalax.file.Path

class CreateTemplateSpec extends Spec with TestHelper {

  describe("CreateTemplate") {
    
    it("better get the absolute position right if the file is in the root directory") {

      val createTemplate = TemplateFactory(
        fileInformation = Map( Path("readme.markdown") -> "example content" ),
        output = Path("build/output"),
        root = Path("."),
        templatePath = "com/tristanhunt/sourcedown/template.ssp"
      ).asInstanceOf[ScalateTemplate]
      
      val toRoot = createTemplate.toRoot( Path("./readme.markdown") )

      assert(toRoot === ".")
    }

    it("should locate relative paths when using . as the root") {
      
      val createTemplate = TemplateFactory(
        fileInformation = Map( Path("src/main/scala/example.scala") -> "example content"),
        output = Path("build/output"),
        root = Path("."),
        templatePath = "com/tristanhunt/sourcedown/template.ssp"
      ).asInstanceOf[ScalateTemplate]
     
      val toRoot = createTemplate.toRoot( Path("src/main/scala/example.scala") )

      assert(toRoot === "../../..")
    }
  }
}