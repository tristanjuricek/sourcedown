/*

# ParseCommentsSpec

This is just a few checks I'm using to speed up my dev cycle. Not an exhaustive
specification.

*/

package com.tristanhunt.sourcedown

import org.scalatest.Spec

class ParseCommentsSpec extends Spec with TestHelper {
  
  describe("ParseComments") {
    
    it("should properly join two single line comments as a group of lines") {
      
      val sections = ParseComments(tempContents(".java",
        """    // **TODO** Fix this
          |    // Example of a second line""".stripMargin))

      assert(sections.head.trim === """**TODO** Fix this
                                      |Example of a second line""".stripMargin)
    }

    it("should ignore comment markers in strings in C Style declarations, " + 
       "even when in a commented out section") {
      
      val sections = ParseComments(tempContents(".c", 
        """void func(int x) {
          |    /* let's pretend this is commented out. It should look funky.
          |    println("x could be // divided or */ whatever");
          |    */
          |    // And this should not be commented out
          |    println("x // 0 is \" almost as strange as /*");
          |}""".stripMargin))

      val comment = sections(1).asInstanceOf[MultiLineComment]
      val code = sections(3).asInstanceOf[Code]

      assert(comment.trim.trim === 
        """let's pretend this is commented out. It should look funky.
          |println("x could be // divided or */ whatever");""".stripMargin)
      
      assert(code.trim.trim === 
        "println(\"x // 0 is \\\" almost as strange as /*\");\n}")
    }
  }
}