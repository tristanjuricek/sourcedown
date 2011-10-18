/*
    # Markdown conversion using knockoff

    The goal of this is an XHTML snippet based on "rebased" markdown. This 
    snippet will probably be applied within a template (somewhere) to create
    the final website file.

    Custom conversion of the XHTML is allowed by assigning a different 
    discounter to the `KnockoffConversion` object.
*/

package com.tristanhunt.sourcedown

import scala.xml.Node
import com.tristanhunt.knockoff.DefaultDiscounter

object KnockoffConversion {
    
  var discounter = DefaultDiscounter

  def toXHTML(markdown: String): Node = {
    discounter.toXHTML( discounter.knockoff(markdown) )
  }
         
}