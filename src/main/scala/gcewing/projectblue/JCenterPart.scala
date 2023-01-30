//------------------------------------------------------------------------------------------------
//
//   Project Blue - Java Center Part
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue

import codechicken.multipart._

abstract class JCenterPart extends TSlottedPart {

  def getSlotMask: Int = 1 << 6

}
