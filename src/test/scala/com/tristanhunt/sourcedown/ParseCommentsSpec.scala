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

      assert(sections.head.trim == """**TODO** Fix this
                                      |Example of a second line""".stripMargin)
    }
  }
}