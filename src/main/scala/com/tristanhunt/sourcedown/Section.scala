/*
  # The Parse Tree of Source Files

  For this version, it's outrageously simple.
*/
package com.tristanhunt.sourcedown

trait Section {
  def source: String
}

case class Code(val source: String) extends Section

case class SingleLineComment(val source: String, val start: String) 
  extends Section

case class MultiLineComment(val source: String, val start: String, 
                            val end: String) extends Section


