// ------------------------------------------------------------------------------------------------
//
// Project Blue - Bundled Signal Utilities
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.tileentity.*;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.*;
import mrtjp.projectred.api.*;
import mrtjp.projectred.core.*;
import mrtjp.projectred.core.libmc.*;

public class BundledUtils {

    static byte[] signal = new byte[16];

    public static byte[] getAllSignals(TMultiPart part, int side) {
        return getAllSignals(part, side, 0x3);
    }

    public static byte[] getAllSignals(TMultiPart part, int side, int connectionMask) {
        clearSignal(signal);
        for (int r = 0; r <= 3; r++) if ((connectionMask & (1 << r)) != 0) {
            addSignal(signal, getCornerSignal(part, side, r));
            addSignal(signal, getStraightSignal(part, side, r));
            addSignal(signal, getInternalSignal(part, side, r));
        }
        addSignal(signal, getCenterSignal(part, side));
        return signal;
    }

    public static void clearSignal(byte[] sig) {
        for (int i = 0; i <= 15; i++) sig[i] = 0;
    }

    public static void addSignal(byte[] dst, byte[] src) {
        if (src != null) for (int i = 0; i <= 15; i++) if ((dst[i] & 0xff) < (src[i] & 0xff)) dst[i] = src[i];
    }

    public static byte[] getCornerSignal(TMultiPart part, int side, int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(part.tile()).offset(absDir).offset(side);
        TileMultipart t = PRLib.getMultipartTile(part.world(), pos);
        if (t != null) return getBundledPartSignal(t.partMap(absDir ^ 1), Rotation.rotationTo(absDir ^ 1, side ^ 1));
        return null;
    }

    public static byte[] getStraightSignal(TMultiPart part, int side, int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(part.tile()).offset(absDir);
        TileEntity t = part.world().getTileEntity(pos.x, pos.y, pos.z);
        if (t instanceof IBundledEmitter) return getBundledPartSignal(t, absDir ^ 1);
        else if (t instanceof TileMultipart)
            return getBundledPartSignal(((TileMultipart) t).partMap(side), (r + 2) % 4);
        return null;
    }

    public static byte[] getInternalSignal(TMultiPart part, int side, int r) {
        int absDir = Rotation.rotateSide(side, r);
        TMultiPart tp = part.tile().partMap(absDir);
        return getBundledPartSignal(tp, Rotation.rotationTo(absDir, side));
    }

    public static byte[] getCenterSignal(TMultiPart part, int side) {
        TMultiPart center = part.tile().partMap(6);
        if (center != part) return getBundledPartSignal(center, side);
        return null;
    }

    public static byte[] getBundledPartSignal(Object part, int r) {
        if (part instanceof IBundledEmitter) return ((IBundledEmitter) part).getBundledSignal(r);
        return null;
    }

}
