/*
    # The Sourcedown Test Runner

    Excecutes sourcedown in various "input" test directories and outputs things
    to a corresponding build folder. Once complete, we run a tree diff tool and
    dump any changes we see.

    This will follow typical project build conventions and thus will not need
    much configuration.
*/

import java.io.File

object RunSourcedownTests {
    
    var testRootFolder = "test/resources/test"
    var inputFolder = "input"
    var expectedFolder = "expected"
    var outputRootFolder = "build/tests"

    def main(args: Array[String]) {
        testRootFolder.listFiles.foreach(executeTest)
    }

    /*
        All tests are executed as an external process. Because that's generally
        how people will use sourcedown. Or that's how I *anticipate* it being
        used.
    */
    def executeTest(testFolder: File) {
        
    }

    implicit def file(path: String): File =
        new File(path)
}