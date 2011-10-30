/*
  # The Sourcedown Test Runner

  Excecutes sourcedown in various "input" test directories and outputs things
  to a corresponding build folder. Once complete, we run a tree diff tool and
  dump any changes we see.

  This will follow typical project build conventions and thus will not need
  much configuration.
*/

package com.tristanhunt.sourcedown

import scalax.file._
import sys.process._

object RunSourcedownTests {
  
  var testRootFolder = "src/test/resources/test"
  var inputFolder = "input"
  var expectedFolder = "expected"
  var outputRootFolder = "build/tests"

  var sourcedownBuild = "build/libs/sourcedown.jar"

  def main(args: Array[String]) {

    testRootFolder.children().foreach(executeTest)
  }

  /*
    All tests are executed as an external process. Because that's generally
    how people will use sourcedown. Or that's how I *anticipate* it being
    used.
  */
  def executeTest(testFolder: Path) {
    Console.println("Running test " + testFolder.name)

    var status = ""
    var ex: Exception = null

    try {
      var outputFolder = prepareOutputFolder(testFolder)

      var execTest = Process("java -jar " + sourcedownBuild + 
                             " -o " + outputFolder +
                             " -d " + testFolder)
  
      var result = execTest.run(true)

      if (result.exitValue != 0) {
        status = Console.RED + "FAIL " + testFolder.name + Console.RESET
      } else {
        status = validateTest(testFolder, outputFolder)
      }
    } catch {
      case e : Exception => {
        status = Console.RED + e.getMessage + Console.RESET
        ex = e
      }
    }
  
    Console.println(status)

    if (ex != null) {
      ex.printStackTrace()
    }
  }

  /*
    Validation is done by running the diff tool. If we get any output then
    the test failed (and dump the damn output)
  */
  def validateTest(testFolder :Path, outputFolder :Path): String = {
    val expected = testFolder / "expected"
    
    var execDiff = Process("diff " + expected.path + " " + outputFolder.path)

    var diffProcess = execDiff.run(true)

    if (diffProcess.exitValue == 0) {
      return Console.GREEN + "OK " + testFolder.name + Console.RESET        
    } else {
      return Console.RED + "FAIL " + testFolder.name + Console.RESET
    }
  }

  /*
    Makes sure the output folder exists for a particular input folder and
    returns the output test folder.
  */
  def prepareOutputFolder(testFolder: Path): Path = {
    var outputFolder = path(outputRootFolder + "/" + testFolder.name)

    if (outputFolder.exists) {
      outputFolder.deleteRecursively(true)
    }

    outputFolder.createDirectory()

    return outputFolder
  }

  implicit def path(path: String): Path = Path(path)
}