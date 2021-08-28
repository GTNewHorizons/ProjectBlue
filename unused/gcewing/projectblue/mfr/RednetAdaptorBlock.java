//------------------------------------------------------------------------------------------------
//
//   Project Blue - Rednet to Bundled Cable Adaptor Block
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue.mfr;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.entity.player.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

import powercrystals.minefactoryreloaded.api.rednet.*;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.*;

import gcewing.projectblue.*;

public class RednetAdaptorBlock extends BaseContainerBlock<RednetAdaptorTE>
	implements IRedNetOmniNode
{

	int[] outputs = new int[16];

	public RednetAdaptorBlock() {
		super(Material.circuits, RednetAdaptorTE.class);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		//System.out.printf("RednetAdaptorBlock.onNeighborBlockChange\n");
		getTileEntity(world, x, y, z).onNeighborBlockChange();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
		EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote) {
			//System.out.printf("RednetAdaptorBlock.onBlockActivated\n");
			ProjectBlue.mod.openGui(player, PBGui.RednetAdaptor, world, x, y, z);
		}
		return true;
	}
	
	//
	//   Rednet
	//

	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z,
		ForgeDirection side)
	{
		return RedNetConnectionType.CableAll;
	}
	
	@Override
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection side,
		int[] inputValues)
	{
		//System.out.printf("RednetAdaptorBlock.onInputsChanged:");
		//for (int i : inputValues)
		//	System.out.printf(" %02x", i);
		//System.out.printf("\n");
		getTileEntity(world, x, y, z).onRednetInputsChanged(inputValues);
	}

	@Override
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection side,
		int inputValue) {}
	
	@Override
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection side) {
		return getTileEntity(world, x, y, z).getRednetOutput();
	}
	
	@Override
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection side,
		int subnet) {return 0;}
	
}
