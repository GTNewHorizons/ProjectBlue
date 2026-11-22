// ------------------------------------------------------------------------------------------------
//
// Project Blue - Java Center Part
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;

public abstract class JCenterPart extends TMultiPart implements TSlottedPart {

    @Override
    public int getSlotMask() {
        return 1 << 6;
    }
}
