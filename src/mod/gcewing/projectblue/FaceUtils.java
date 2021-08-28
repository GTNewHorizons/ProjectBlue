//------------------------------------------------------------------------------------------------
//
//	 Project Blue - Face Part Utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.tileentity.*;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.*;
import mrtjp.projectred.api.*;
import mrtjp.projectred.core.*;

public class FaceUtils {

	public static void notifyAllNeighbors(TMultiPart part, int side) {
		part.tile().notifyPartChange(part);
		notifyAllCorners(part, side);
	}

	public static void notifyAllCorners(TMultiPart part, int side) {
		for (int r = 0; r <= 3; r++)
			notifyCorner(part, side, r);
	}
	
	public static void notifyCorner(TMultiPart part, int side, int r) {
		int absDir = Rotation.rotateSide(side, r);
		TileMultipart tile = part.tile();
		BlockCoord pos = new BlockCoord(tile).offset(absDir).offset(side);
		part.world().notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, ((TileEntity)tile).getBlockType());
	}

}
