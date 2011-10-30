package com.tristanhunt.sourcedown

import scalax.file.Path

trait TestHelper {
  
  def tempContents(extension: String, contents:String): Path = {
    val path = Path.createTempFile(suffix = extension)
    path.write(contents)
    return path
  }
}