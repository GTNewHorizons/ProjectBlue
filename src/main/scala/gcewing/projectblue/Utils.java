// ------------------------------------------------------------------------------------------------
//
// Project Blue - Utilities
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.util.*;

import codechicken.lib.vec.Vector3;

public class Utils {

    public static Vector3 ccVector3(double x, double y, double z) {
        return new Vector3(x, y, z);
    }

    public static Vector3 ccVector3(Vec3 v) {
        return new Vector3(v);
    }

}
